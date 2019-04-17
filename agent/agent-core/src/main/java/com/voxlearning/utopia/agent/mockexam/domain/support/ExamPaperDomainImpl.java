package com.voxlearning.utopia.agent.mockexam.domain.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPaperDao;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPaperProcessDao;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPaperProcessStateDao;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPlanDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperEntity;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperProcessEntity;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperProcessStatusEntity;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import com.voxlearning.utopia.agent.mockexam.domain.ExamPaperDomain;
import com.voxlearning.utopia.agent.mockexam.domain.ExamPlanDomain;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaper;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaperProcessState;
import com.voxlearning.utopia.agent.mockexam.integration.ExamPaperClient;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.BooleanEnum;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperOpenOptionParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperOpenOptionResult;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 试卷领域服务实现
 *
 * @author xiaolei.li
 * @version 2018/8/6
 */
@Slf4j
@Service
public class ExamPaperDomainImpl implements ExamPaperDomain {

    @Resource
    ExamPaperDao examPaperDao;

    @Resource
    ExamPaperProcessDao paperProcessDao;

    @Resource
    ExamPaperProcessStateDao paperProcessStatusDao;

    @Resource
    ExamPlanDomain examPlanDomain;

    @Resource
    ExamPaperClient paperClient;

    @Resource
    ExamPlanDao examPlanDao;

    @Inject
    protected BaseOrgService baseOrgService;

    @Resource
    ExamPaperClient examPaperClient;

    @Override
    public void create(ExamPlanEntity plan) {

        // step 1 : 发起创建新试卷流程
        ExamPaperClient.CreateRequest request = ExamPaperClient.CreateRequest.Builder.build(plan);
        ExamPaperClient.CreateResponse response = paperClient.create(request);
        if (!response.isSuccess()) {
            throw new BusinessException(ErrorCode.EXAM_CREATE, response.getErrorMessage());
        }

        // step 2 : 存储试卷流程初始状态
        ExamPaperClient.ProcessState state = response.getData();
        ExamPaperProcessStatusEntity _state = new ExamPaperProcessStatusEntity();
        _state.setProcessId(state.getProcessId());
        _state.setStatus(state.getState());
        paperProcessStatusDao.insert(_state);

        // step 3 : 存储试卷流程
        // 现根据processId查询是否已经存在流程
        ExamPaperProcessEntity _process = paperProcessDao.findByProcessId(state.getProcessId());
        if (null != _process) {
            // 已有流程 => 更新状态
            ExamPaperProcessEntity _entity = new ExamPaperProcessEntity();
            _entity.setId(_process.getId());
            _entity.setStatus(state.getState());
            paperProcessDao.update(_entity);
        } else {
            // 新流程 => 创建流程
            ExamPaperProcessEntity process = new ExamPaperProcessEntity();
            process.setPlanId(Long.valueOf(state.getBusinessId()));
            process.setProcessId(state.getProcessId());
            process.setStatus(state.getState());
            process.setDisable(BooleanEnum.N.name());
            paperProcessDao.insert(process);
        }
    }

    @Override
    public ExamPaper retrieve(String paperId) {
        // 调用题库侧接口，查询试卷
        ExamPaperClient.PaperRequest request = new ExamPaperClient.PaperRequest();
        request.setPaperId(paperId);
        request.setPage(1);
        ExamPaperClient.PaperPageResponse response = examPaperClient.queryPage(request);
        List<ExamPaper> papers = ExamPaperClient.PaperPageResponse.Builder.build(response);
        return null;
    }

    @Override
    public void handleNotify(ExamPaperProcessState state) {

        // step 1 : 试卷流程状态
        // 每次通知保存一个状态，按照时间倒序查询列表，就可以获得试卷的流程状态
        ExamPaperProcessStatusEntity notify = ExamPaperProcessState.Builder.build(state);
        paperProcessStatusDao.insert(notify);

        // step 2 : 更新流程状态
        // 根据processId查找流程记录
        ExamPaperProcessEntity entity = paperProcessDao.findByProcessId(state.getProcessId());
        if (null == entity)
            throw new BusinessException(ErrorCode.PAPER_NOTIFY_STATE,
                    String.format("流程通知时发生错误，没有对一个的流程，notify = %s", state));
        else {
            ExamPaperProcessEntity _entity = new ExamPaperProcessEntity();
            _entity.setId(entity.getId());
            _entity.setProcessId(state.getProcessId());
            _entity.setStatus(state.getStatus().name());
            paperProcessDao.update(_entity);
        }

        // step 3 : 试卷状态级联计划状态
        examPlanDomain.handlePaperStatus(state);
    }


    @Override
    public PageResult<ExamPaper> queryPage(ExamPaperQueryParams params, PageInfo pageInfo) {

        // 调用题库侧接口，查询试卷
        ExamPaperClient.PaperRequest request = ExamPaperClient.PaperRequest.Builder.build(params);
        request.setPage(pageInfo.getPage());
        ExamPaperClient.PaperPageResponse response = examPaperClient.queryPage(request);
        List<ExamPaper> papers = ExamPaperClient.PaperPageResponse.Builder.build(response);

        // 组合本地数据
        if (null != papers && !papers.isEmpty()) {
            Set<String> paperIds = papers.stream().map(ExamPaper::getPaperId).collect(Collectors.toSet());
            Map<String, ExamPaperEntity> localPapers = examPaperDao.findByPaperId(paperIds);
            papers.stream().forEach(i -> {
                final String paperId = i.getPaperId();
                if (localPapers.containsKey(paperId)) {
                    // 从本地数据中赋值引用次数和是否公开
                    ExamPaperEntity local = localPapers.get(paperId);
                    i.setPlanTimes(local.getPlanTimes());
                    i.setIsPublic(local.getIsPublic());
                    ExamPlanEnums.Form from = ExamPlanEnums.Form.of(local.getPlanForm());
                    i.setPlanForm(Objects.isNull(from) ? null:from.desc);
                } else {
                    i.setPlanTimes(0);
                    if (i.getSource().equals("通用"))
                        i.setIsPublic(BooleanEnum.Y.name());
                    else
                        i.setIsPublic(BooleanEnum.N.name());
                }
            });
        }
        return PageResult.success((ArrayList) papers, pageInfo, response.getTotal());
    }

    @Override
    public Result<ExamPaper> queryOne(ExamPaperQueryParams params) {
        ExamPaperClient.PaperRequest request = new ExamPaperClient.PaperRequest();
        request.setPaperId(params.getPaperId());
        request.setPage(1);
        request.setSubject(ExamPlanEnums.Subject.of(params.getSubject()));
        ExamPaperClient.PaperPageResponse response = examPaperClient.queryPage(request);
        List<ExamPaper> papers = ExamPaperClient.PaperPageResponse.Builder.build(response);

        ExamPaper examPaper = new ExamPaper();

        if (CollectionUtils.isNotEmpty(papers)) {
            examPaper = Optional.ofNullable(papers.get(0)).orElse(new ExamPaper());
        }
        return Result.success(examPaper);
    }

    @Override
    public ExamPaperOpenOptionResult openOrClose(ExamPaperOpenOptionParams params) {
        final String paperId = params.getPaperId();
        if (StringUtils.isBlank(paperId))
            throw new BusinessException(ErrorCode.PAPER_OPEN_CLOSE, "请输入试卷id");
        ExamPaperEntity entity = examPaperDao.findByPaperId(paperId);
        ExamPaperOpenOptionResult result = new ExamPaperOpenOptionResult();
        result.setPaperId(paperId);
        if (null == entity) {
            entity = new ExamPaperEntity();
            entity.setPaperId(paperId);
            entity.setPlanTimes(0);
            entity.setIsPublic(BooleanEnum.Y.name());
            result.setIsPublic(BooleanEnum.Y);
            examPaperDao.insert(entity);
            result.setId(entity.getId());
        } else {
            ExamPaperEntity _entity = new ExamPaperEntity();
            _entity.setId(entity.getId());
            String isPublic = entity.getIsPublic();
            if (BooleanEnum.Y.name().equals(isPublic)) {
                _entity.setIsPublic(BooleanEnum.N.name());
                result.setIsPublic(BooleanEnum.N);
            } else {
                _entity.setIsPublic(BooleanEnum.Y.name());
                result.setIsPublic(BooleanEnum.Y);
            }
            examPaperDao.update(_entity);
            result.setId(entity.getId());
        }
        return result;
    }

    @Override
    public long count(ExamPaperQueryParams params) {
        return examPaperDao.count(params);
    }

    @Override
    public void increaseReference(String paperId, String planForm, String subject) {
        if (StringUtils.isBlank(paperId))
            throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR, "请提供试卷id");
        ExamPaperEntity entity = examPaperDao.findByPaperId(paperId);
        if (null == entity) {
            // 没有此试卷信息 => 新增记录
            entity = new ExamPaperEntity();
            entity.setPaperId(paperId);
            entity.setPlanTimes(1);
            entity.setPlanForm(planForm);
            // 查试卷详情
            List<ExamPaper> papers = queryPaperInfo(paperId,subject);
            if ( CollectionUtils.isNotEmpty(papers) ){
                entity.setIsPublic(papers.get(0).getSource().equals("通用")
                        ? BooleanEnum.Y.name() : BooleanEnum.N.name());
            } else {
                throw new BusinessException(ErrorCode.PAPER_NOT_EXIST_ERROR, "试卷信息不存在");
            }
            examPaperDao.insert(entity);
        } else {
            // 已有试卷 => 引用计数+1
            ExamPaperEntity _entity = new ExamPaperEntity();
            _entity.setId(entity.getId());
            if(entity.getPlanTimes() < 0){
                _entity.setPlanTimes(1);
            }else {
                _entity.setPlanTimes(entity.getPlanTimes() + 1);
            }
            //试卷的测评形式以第一次上线的测评为准，所以如果已经存在测评形式了就以已存在的为准
            if (StringUtils.isBlank(entity.getPlanForm())) {
                _entity.setPlanForm(planForm);
            }
            examPaperDao.update(_entity);
        }
    }

    @Override
    public void decreaseReference(String paperId,String subject) {
        if (StringUtils.isBlank(paperId))
            throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR, "请提供试卷id");
        ExamPaperEntity entity = examPaperDao.findByPaperId(paperId);
        if (null == entity) {
            // 没有此试卷信息 => 新增记录
            entity = new ExamPaperEntity();
            entity.setPaperId(paperId);
            entity.setPlanTimes(0);
            // 查试卷详情
            List<ExamPaper> papers = queryPaperInfo(paperId,subject);
            if ( CollectionUtils.isNotEmpty(papers) ){
                entity.setIsPublic(papers.get(0).getSource().equals("通用")
                        ? BooleanEnum.Y.name() : BooleanEnum.N.name());
            } else {
                throw new BusinessException(ErrorCode.PAPER_NOT_EXIST_ERROR, "试卷信息不存在");
            }
            examPaperDao.insert(entity);
        } else {
            // 已有试卷 => 引用计数-1
            ExamPaperEntity _entity = new ExamPaperEntity();
            _entity.setId(entity.getId());
            if(entity.getPlanTimes() <= 0){
                _entity.setPlanTimes(0);
            }else {
                _entity.setPlanTimes(entity.getPlanTimes() - 1);
            }
            examPaperDao.update(_entity);
        }
    }

    public List<ExamPaper> queryPaperInfo(String paperId,String subject){
        ExamPaperClient.PaperRequest request = new ExamPaperClient.PaperRequest();
        request.setPaperId(paperId);
        request.setPage(1);
        request.setSubject(ExamPlanEnums.Subject.of(subject));
        ExamPaperClient.PaperPageResponse response = examPaperClient.queryPage(request);
        List<ExamPaper> papers = ExamPaperClient.PaperPageResponse.Builder.build(response);
        return papers;
    }
}
