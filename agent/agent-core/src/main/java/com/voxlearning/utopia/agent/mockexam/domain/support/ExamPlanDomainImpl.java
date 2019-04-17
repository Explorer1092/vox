package com.voxlearning.utopia.agent.mockexam.domain.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPlanDao;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPlanOperateLogDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanOperateLogEntity;
import com.voxlearning.utopia.agent.mockexam.domain.ExamNotifyDomain;
import com.voxlearning.utopia.agent.mockexam.domain.ExamPaperDomain;
import com.voxlearning.utopia.agent.mockexam.domain.ExamPlanDomain;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaper;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaperProcessState;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlan;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlanOperateLog;
import com.voxlearning.utopia.agent.mockexam.domain.validate.ValidateResult;
import com.voxlearning.utopia.agent.mockexam.domain.validate.ValidateUtil;
import com.voxlearning.utopia.agent.mockexam.integration.ExamClient;
import com.voxlearning.utopia.agent.mockexam.integration.ExamPaperClient;
import com.voxlearning.utopia.agent.mockexam.integration.ExamReportClient;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.OperateRequest;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPaperEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.PaperType;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.Status;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.*;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ReportExistDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.Status.PAPER_PROCESSING;
import static com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.Status.PAPER_REJECT;
import static com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.Status.PLAN_REJECT;

/**
 * 考试计划领域层实现
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Slf4j
@Service
public class ExamPlanDomainImpl implements ExamPlanDomain {

    @Resource
    ExamPlanDao examPlanDao;

    @Resource
    ExamPlanOperateLogDao planOperateLogDao;

    @Resource
    ExamClient examClient;

    @Resource
    ExamPaperDomain examPaperDomain;

    @Resource
    ExamPlanChecker examPlanChecker;

    @Resource
    NotifyManager notifyManager;

    @Resource
    ExamPlan.Builder builder;

    @Resource
    ExamClient.ApplyRequest.Builder applyBuilder;

    @Resource
    ExamReportClient examReportClient;

    @Resource
    ExamPaperClient paperClient;

    @Override
    public void create(ExamPlan model) {
        Date now = new Date();
        model.setCreateDatetime(now);
        model.setUpdateDatetime(now);
        model.setStatus(Status.PLAN_AUDITING);
        examPlanChecker.check(model);
        ExamPlanEntity entity = ExamPlanEntity.Builder.build(model);
        examPlanDao.insert(entity);
    }


    @Override
    public void update(ExamPlan model) {

        // 有些值是不需要赋值的，这里进行重新覆盖
        ExamPlanEntity _entity = examPlanDao.findById(model.getId());
        model.setCreatorId(_entity.getCreatorId());
        model.setCreatorName(_entity.getCreatorName());
        model.setCreateDatetime(_entity.getCreateDatetime());
        Date now = new Date();
        model.setUpdateDatetime(now);
        model.setStatus(Status.valueOf(_entity.getStatus()));

        examPlanChecker.check(model);
        ExamPlanEntity entity = ExamPlanEntity.Builder.build(model);
        examPlanDao.update(entity);
    }

    @Override
    public void submit(ExamPlan model) {
        Long id = model.getId();
        ExamPlanEntity prePlan = examPlanDao.findById(id);
        this.check(model);

        if (null == id) {
            // 没有主键 => 创建
            Date now = new Date();
            model.setCreatorId(model.getOperatorId());
            model.setCreatorName(model.getOperatorName());
            model.setCreateDatetime(now);
            model.setUpdateDatetime(now);
            if (model.isAdmin()) {
                // 管理员提交不经过审核
                if (PaperType.OLD == model.getPaperType()) {
                    model.setStatus(Status.PAPER_READY);
                    examPlanChecker.check(model);
                    ExamPlanEntity entity = ExamPlanEntity.Builder.build(model);
                    examPlanDao.insert(entity);
                    id = entity.getId();
                } else {
                    // 管理员 => 先置为审核中创建测评记录
                    model.setStatus(Status.PLAN_AUDITING);
                    examPlanChecker.check(model);
                    ExamPlanEntity entity = ExamPlanEntity.Builder.build(model);
                    examPlanDao.insert(entity);
                    // 创建试卷流程
                    model.setId(entity.getId());
                    examPaperDomain.create(ExamPlanEntity.Builder.build(model));
                    // 更新测评计划
                    ExamPlanEntity _entity = new ExamPlanEntity();
                    _entity.setId(entity.getId());
                    _entity.setStatus(Status.PAPER_CHECKING.name());
                    _entity.setUpdateDatetime(new Date());
                    examPlanDao.update(_entity);
                    id = entity.getId();
                }
            } else {
                // 普通用户提交需要审核
                model.setStatus(Status.PLAN_AUDITING);
                examPlanChecker.check(model);
                ExamPlanEntity entity = ExamPlanEntity.Builder.build(model);
                examPlanDao.insert(entity);
                id = entity.getId();
            }

        } else {
            // 有主键 => 根据之前的状态进行分别处理
            if (null == prePlan)
                throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR,
                        String.format("根据id[%s]没有查询到测评记录", id));
            Date now = new Date();
            model.setUpdateDatetime(now);
            model.setCreatorId(prePlan.getCreatorId());
            model.setCreatorName(prePlan.getCreatorName());
            model.setCreateDatetime(prePlan.getCreateDatetime());

            Status status = Status.valueOf(prePlan.getStatus());
            model.setStatus(status);
            examPlanChecker.check(model);
            switch (status) {
                case PLAN_REJECT:
                case PLAN_WITHDRAW:
                case PAPER_REJECT: {
                    if (model.isAdmin()) {
                        // 管理员提交不经过审核
                        if (PaperType.OLD == model.getPaperType())
                            model.setStatus(Status.PAPER_READY);
                        else {
                            examPaperDomain.create(ExamPlanEntity.Builder.build(model));
                            model.setStatus(Status.PAPER_CHECKING);
                        }
                    } else
                        // 普通用户提交需要审核
                        model.setStatus(Status.PLAN_AUDITING);
                    break;
                }
                case PLAN_AUDITING:
                case EXAM_OFFLINE:
                    // 上面几种前置状态修改操作不会改变状态
                    break;
                case EXAM_PUBLISHED:
                    ExamClient.ApplyResponse response = examClient.apply(model);
                    if (!response.isSuccess()) {
                        throw new BusinessException(ErrorCode.EXAM_ONLINE,
                                String.format("更新数据发生错误,%s", StringUtils.isNotBlank(response.getInfo()) ? response.getInfo() : ""));
                    }
                    break;
                case PAPER_CHECKING:
                case PAPER_PROCESSING:
                case PAPER_READY:
                    if (PaperType.NEW == model.getPaperType()){
                        ExamPaperClient.CreateRequest paperRequest = ExamPaperClient.CreateRequest.Builder.
                                buildBoth(prePlan, ExamPlanEntity.Builder.build(model));
                        ExamPaperClient.CreateResponse paperResponse = paperClient.create(paperRequest);
                        if (!paperResponse.isSuccess()) {
                            throw new BusinessException(ErrorCode.EXAM_UPDATE,
                                    String.format("更新数据发生错误,%s", StringUtils.isNotBlank(paperResponse.getErrorMessage())
                                            ? paperResponse.getErrorMessage() : ""));
                        }
                    }
                    break;
                default:
                    throw new BusinessException(ErrorCode.PLAN_SUBMIT,
                            String.format("尚未处理的测评前置状态[id=%s][status=%s]", id, status));
            }
            ExamPlanEntity entity = ExamPlanEntity.Builder.build(model);
            examPlanDao.update(entity);
        }
        notifyManager.notify(prePlan, examPlanDao.findById(id));
    }

    @Override
    public void withdraw(OperateRequest params) {
        final Long planId = params.getId();

        if (planId == null)
            throw new BusinessException(ErrorCode.EXAM_WITHDRAW,
                    "没有指定要撤销的考试计划ID");

        ExamPlanEntity plan = examPlanDao.findById(planId);
        if(plan == null){
            throw new BusinessException(ErrorCode.EXAM_WITHDRAW,
                    "考试计划不存在");
        }

        Status prevStatus = Status.valueOf(plan.getStatus());

        // 试卷审核中、试卷被驳回、试卷录入中、试卷已录入”状态下， 管理员可以撤回
        if(prevStatus == Status.PAPER_CHECKING
                || prevStatus == Status.PAPER_REJECT
                || prevStatus == Status.PAPER_PROCESSING
                || prevStatus == Status.PAPER_READY){
            if(params.isAdmin()){
                ExamClient.WithdrawRequest request = ExamClient.WithdrawRequest.Builder.build(String.valueOf(planId));
                ExamClient.WithdrawResponse response = examClient.withdraw(request);
                if (!response.isSuccess()) {
                    throw new BusinessException(ErrorCode.EXAM_WITHDRAW, StringUtils.isNotBlank(response.getInfo()) ? response.getInfo() : ErrorCode.EXAM_WITHDRAW.desc);
                }
            }else {
                throw new BusinessException(ErrorCode.EXAM_WITHDRAW, "您无该状态的撤销权限！");
            }
        }

        plan.setStatus(Status.PLAN_WITHDRAW.name());
        examPlanDao.update(plan);

        ExamPlanOperateLogEntity log = new ExamPlanOperateLogEntity();
        log.setPlanId(planId);
        log.setPrevStatus(prevStatus.name());
        log.setCurrentStatus(Status.PLAN_WITHDRAW.name());
        log.setOperatorId(params.getOperatorId());
        log.setOperatorName(params.getOperatorName());
        log.setNote("撤销测评");
        planOperateLogDao.insert(log);
    }

    @Override
    public ExamPlan retrieve(Long id) {
        ExamPlanEntity entity = examPlanDao.findById(id);
        ExamPlan model = builder.build(entity);
        List<ExamPlanOperateLogEntity> logs = planOperateLogDao.findByPlanId(id);
        model.setLogs(logs.stream().map(ExamPlanOperateLog.Builder::build)
                .collect(Collectors.toList()));
        return model;
    }

    @Override
    public long count(ExamPlanQueryParams params) {
        return examPlanDao.count(params);
    }

    @Override
    public List<ExamPlan> query(ExamPlanQueryParams params, PageInfo pageInfo) {
        pageInfo = pageInfo.getDefaultValueIfInvalid();
        List<ExamPlanEntity> entities = examPlanDao.query(params, pageInfo);
        return entities.stream().map(i -> builder.build(i)).collect(Collectors.toList());
    }

    @Override
    public void handlePaperStatus(ExamPaperProcessState state) {

        // 前置条件 : 确保测评记录正确
        if (StringUtils.isBlank(state.getBusinessId()))
            throw new BusinessException(ErrorCode.PAPER_NOTIFY_STATE,
                    String.format("试卷流程状态通知处理时,测评id错误,state = %s", state));
        final Long planId = Long.valueOf(state.getBusinessId());
        ExamPlanEntity prevPlan = examPlanDao.findById(planId);
        if (prevPlan == null) {
            throw new BusinessException(ErrorCode.PAPER_NOTIFY_STATE,
                    String.format("试卷流程状态通知处理时,无法找到对应的测评记录,state = %s", state));
        }
        // 用于持久化的实体
        ExamPlanEntity entity = new ExamPlanEntity();
        entity.setId(planId);
        switch (state.getStatus()) {
            case REJECT: {
                entity.setStatus(PAPER_REJECT.name());
                // 操作日志
                ExamPlanOperateLogEntity log = new ExamPlanOperateLogEntity();
                log.setPlanId(planId);
                log.setPrevStatus(prevPlan.getStatus());
                log.setCurrentStatus(PAPER_REJECT.name());
                log.setOperatorId(0L);
                log.setOperatorName(state.getOperatorName());
                log.setNote(state.getRejectReason());
                planOperateLogDao.insert(log);
                break;
            }
            case DONE: {
                entity.setStatus(Status.PAPER_READY.name());
                // 填充试卷id
                List<ExamPaperProcessStateNotify.Paper> papers = state.getPapers();
                List<ExamPlan.Paper> _papers = JSON.parseArray(prevPlan.getPapers(), ExamPlan.Paper.class);
                if (papers.size() != _papers.size()) {
                    throw new BusinessException(ErrorCode.PAPER_NOTIFY_STATE,
                            String.format("试卷流程状态通知处理时,试卷和文档数量不正确,state = %s", state));
                }
                // 按照文档url匹配
                Map<String, ExamPlan.Paper> _paperMap = _papers.stream().collect(Collectors.toMap(ExamPlan.Paper::getDocUrl, i -> i));
                papers.forEach(i -> {
                    if (_paperMap.containsKey(i.getDocUrl())) {
                        _paperMap.get(i.getDocUrl()).setPaperId(i.getPaperId());
                    }
                });
                entity.setPapers(JSON.toJSONString(_papers));
                break;
            }
            case PROCESSING: {
                entity.setStatus(PAPER_PROCESSING.name());
                // 操作日志
                ExamPlanOperateLogEntity log = new ExamPlanOperateLogEntity();
                log.setPlanId(planId);
                log.setPrevStatus(prevPlan.getStatus());
                log.setCurrentStatus(PAPER_PROCESSING.name());
                log.setOperatorId(0L);
                log.setOperatorName(state.getOperatorName());
                log.setNote("");
                planOperateLogDao.insert(log);
                break;
            }
            default:
                throw new BusinessException(ErrorCode.PAPER_NOTIFY_STATE,
                        String.format("试卷流程状态错误,state = %s", state));
        }
        examPlanDao.update(entity);

        notifyManager.notify(prevPlan, examPlanDao.findById(planId));
    }

    @Override
    public void audit(ExamPlanAuditParams params) {

        // 前置条件校验
        if (null != params && null != params.getNote() && params.getNote().length() > 100) {
            throw new BusinessException(ErrorCode.PLAN_CONSTRAINT, "审核意见太长了，不可多于100个字符");
        }

        final Long planId = params.getId();
        ExamPlanEntity prevPlan = examPlanDao.findById(planId);
        ExamPlanEntity _plan = new ExamPlanEntity();
        if (ExamPlanAuditParams.Option.APPROVE == params.getOption()) {
            if (PaperType.NEW.name().equals(prevPlan.getPaperType())) {
                examPaperDomain.create(prevPlan);
                _plan.setStatus(Status.PAPER_CHECKING.name());
            } else {
                // 已有试卷申请创建考试发生异常
                _plan.setStatus(Status.PAPER_READY.name());
            }
        } else {
            _plan.setStatus(PLAN_REJECT.name());
        }
        _plan.setId(planId);
        Date now = new Date();
        _plan.setUpdateDatetime(now);
        examPlanDao.update(_plan);

        // 操作日志
        ExamPlanOperateLogEntity log = new ExamPlanOperateLogEntity();
        log.setPlanId(planId);
        log.setPrevStatus(prevPlan.getStatus());
        log.setCurrentStatus(_plan.getStatus());
        log.setOperatorId(params.getOperatorId());
        log.setOperatorName(params.getOperatorName());
        log.setNote(params.getNote());
        log.setCreateDatetime(now);
        planOperateLogDao.insert(log);

        // 通知
        notifyManager.notify(prevPlan, examPlanDao.findById(planId));
    }

    @Override
    public void online(OperateRequest params) {
        final Long planId = params.getId();
        Date now = new Date();
        ExamPlanEntity entity = examPlanDao.findById(planId);
        ExamPlan plan = builder.build(entity);
        if (StringUtils.isBlank(entity.getExamId())) {
            // 分支一 : 没有考试id => 尚未创建考试 => 调用创建考试接口
            ExamClient.ApplyResponse response = examClient.apply(plan);
            if (!response.isSuccess()) {
                throw new BusinessException(ErrorCode.EXAM_CREATE,
                        String.format("创建试卷时发生错误,%s", StringUtils.isNotBlank(response.getInfo()) ? response.getInfo() : ""));
            } else {
                final String examId = response.getData();
                if (StringUtils.isBlank(examId))
                    throw new BusinessException(ErrorCode.EXAM_CREATE,
                            String.format("发布考试成功，返回结果没有考试id,planId = %s", planId));
                ExamPlanEntity _entity = new ExamPlanEntity();
                _entity.setId(planId);
                _entity.setExamId(examId);
                _entity.setUpdateDatetime(now);
                _entity.setStatus(Status.EXAM_PUBLISHED.name());
                examPlanDao.update(_entity);
            }
        } else {
            // 分支二 : 有考试id => 再次上线 => 调用上线接口
            // 由 online 接口改为申请考试接口,申请考试就相当于上线,状态已经写死成 ‘ONLINE’ 了
            // 转换时间
            plan.setEndTime(new Date(plan.getEndTime().getTime()));
            plan.setScorePublishTime(new Date(plan.getScorePublishTime().getTime()));
            plan.setStartTime(new Date(plan.getStartTime().getTime()));
            plan.setTeacherMarkDeadline(new Date(plan.getTeacherMarkDeadline().getTime()));
            plan.setCreateDatetime(new Date(plan.getCreateDatetime().getTime()));
            plan.setTeacherQueryTime(new Date(plan.getTeacherQueryTime().getTime()));
            plan.setUpdateDatetime(new Date(plan.getUpdateDatetime().getTime()));
            ExamClient.ApplyRequest request = applyBuilder.build(plan);
            ExamClient.ApplyResponse response = examClient.apply(plan);
            if (!response.isSuccess()) {
                throw new BusinessException(ErrorCode.EXAM_ONLINE,
                        String.format("测评上线发生错误,%s", StringUtils.isNotBlank(response.getInfo()) ? response.getInfo() : ""));
            } else {
                ExamPlanEntity _entity = new ExamPlanEntity();
                _entity.setId(planId);
                _entity.setUpdateDatetime(now);
                _entity.setStatus(Status.EXAM_PUBLISHED.name());
                examPlanDao.update(_entity);
            }
        }

        // 试卷引用计数
        ExamPlanEntity _entity = examPlanDao.findById(planId);
        if (StringUtils.isNotBlank(_entity.getPapers())) {
            List<ExamPlan.Paper> papers = JSONArray.parseArray(_entity.getPapers(), ExamPlan.Paper.class);
            papers.forEach(i -> examPaperDomain.increaseReference(i.getPaperId(), _entity.getForm(), _entity.getSubject()));
        }

        // 操作日志
        ExamPlanEntity currentEntity = examPlanDao.findById(planId);
        ExamPlanOperateLogEntity log = new ExamPlanOperateLogEntity();
        log.setPlanId(params.getId());
        log.setPrevStatus(entity.getStatus());
        log.setCurrentStatus(currentEntity.getStatus());
        log.setOperatorId(params.getOperatorId());
        log.setOperatorName(params.getOperatorName());
        log.setCreateDatetime(now);
        planOperateLogDao.insert(log);

        // 通知
        notifyManager.notify(entity, examPlanDao.findById(planId));
    }

    @Override
    public void offline(OperateRequest params) {
        final Long planId = params.getId();
        ExamPlanEntity prevEntity = examPlanDao.findById(planId);
        final String examId = prevEntity.getExamId();
        Date now = new Date();
        if (StringUtils.isBlank(examId))
            throw new BusinessException(ErrorCode.EXAM_OFFLINE,
                    String.format("考试下线关联的计划没有考试id,planId = %s", planId));
        ExamClient.OfflineRequest request = ExamClient.OfflineRequest.Builder.build(examId);
        ExamClient.OfflineResponse response = examClient.offline(request);
        if (!response.isSuccess()) {
            throw new BusinessException(ErrorCode.EXAM_OFFLINE);
        } else {
            ExamPlanEntity _entity = new ExamPlanEntity();
            _entity.setId(planId);
            _entity.setUpdateDatetime(now);
            _entity.setStatus(Status.EXAM_OFFLINE.name());
            examPlanDao.update(_entity);
        }

        // 试卷引用计数
        ExamPlanEntity currentEntity = examPlanDao.findById(planId);
        if (StringUtils.isNotBlank(currentEntity.getPapers())) {
            List<ExamPlan.Paper> papers = JSONArray.parseArray(currentEntity.getPapers(), ExamPlan.Paper.class);
            papers.forEach(i -> examPaperDomain.decreaseReference(i.getPaperId(),currentEntity.getSubject()));
        }

        // 操作日志
        ExamPlanOperateLogEntity log = new ExamPlanOperateLogEntity();
        log.setPlanId(planId);
        log.setPrevStatus(prevEntity.getStatus());
        log.setCurrentStatus(currentEntity.getStatus());
        log.setOperatorId(params.getOperatorId());
        log.setOperatorName(params.getOperatorName());
        log.setCreateDatetime(now);
        planOperateLogDao.insert(log);

        // 通知
        notifyManager.notify(prevEntity, currentEntity);
    }

    @Override
    public ExamPlan getDefaultPlan() {
        return ExamPlan.Builder.buildDefault();
    }

    @Override
    public List<String> getCreatorName(String creatorName) {
        ExamPlanQueryParams params = new ExamPlanQueryParams();
        params.setCreatorName(creatorName);
        List<ExamPlanEntity> entityList = examPlanDao.query(params);
        return entityList.stream()
                .map(ExamPlanEntity::getCreatorName)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 领域模型校验
     */
    @Service
    public static class ExamPlanChecker {

        @Resource
        ExamPaperClient examPaperClient;

        /**
         * 校验
         *
         * @param plan 测评计划
         */
        public void check(ExamPlan plan) {

            // step 1 : 试卷本地校验

            // 基础校验
            ValidateResult validateResult = ValidateUtil.validate(plan);
            if (!validateResult.isSuccess())
                throw new BusinessException(ErrorCode.PLAN_CONSTRAINT, validateResult.getMessage());

            // 如果学科是数学，只能关联一个试卷或者试卷文档
            if (ExamPlanEnums.Subject.MATH == plan.getSubject())
                if (null == plan.getPapers() || plan.getPapers().size() > 1)
                    throw new BusinessException(ErrorCode.PLAN_CONSTRAINT, "数学学科只能关联一个试卷或者试卷文档");

            // 地区校验
            if (ExamPlanEnums.RegionLevel.PROVINCE == plan.getRegionLevel() && (null == plan.getRegions() || plan.getRegions().isEmpty()))
                throw new BusinessException(ErrorCode.PLAN_CONSTRAINT, "省份不可为空");
            if (ExamPlanEnums.RegionLevel.CITY == plan.getRegionLevel() && (null == plan.getRegions() || plan.getRegions().isEmpty()))
                throw new BusinessException(ErrorCode.PLAN_CONSTRAINT, "城市不可为空");
            if (ExamPlanEnums.RegionLevel.COUNTY == plan.getRegionLevel() && (null == plan.getRegions() || plan.getRegions().isEmpty()))
                throw new BusinessException(ErrorCode.PLAN_CONSTRAINT, "地区不可为空");
            if (ExamPlanEnums.RegionLevel.SCHOOL == plan.getRegionLevel() && (null == plan.getRegions() || plan.getRegions().isEmpty()))
                throw new BusinessException(ErrorCode.PLAN_CONSTRAINT, "学校不可为空");


            if (PaperType.OLD == plan.getPaperType()) {
                // 已有试卷 => 查询试卷的有效性
                List<String> paperIds = plan.getPapers().stream().map(ExamPlan.Paper::getPaperId).collect(Collectors.toList());
                if (null == paperIds || paperIds.isEmpty())
                    throw new BusinessException(ErrorCode.PLAN_CONSTRAINT, "没有关联的试卷");
                for (Map.Entry<String, ExamPaperClient.PaperInfo> i : examPaperClient.queryByIds(plan.getSubject(), paperIds).entrySet()) {
                    ExamPaperClient.PaperInfo paper = i.getValue();
                    if (null == paper)
                        throw new BusinessException(ErrorCode.PLAN_CONSTRAINT, String.format("试卷[id = %s]不正确", i.getKey()));
                    else {
                        Date createAt;
                        Date deadline;
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            createAt = simpleDateFormat.parse(paper.getCreated_at());
                            deadline = simpleDateFormat.parse("2017-12-01 00:00:00");
                        } catch (ParseException e) {
                            throw new BusinessException(ErrorCode.PLAN_CONSTRAINT,
                                    "该试卷的创建日期未知，请选择其他试卷");
                        }
//                        if (!Objects.equals(plan.getTotalScore(), paper.getTotal_score()))
//                            throw new BusinessException(ErrorCode.PLAN_CONSTRAINT,
//                                    String.format("所关联试卷“试卷总分”为【%s】，测评记录中“试卷总分”为【%s】", paper.getTotal_score(), plan.getTotalScore()));
                        if (createAt.getTime() <= deadline.getTime())
                            throw new BusinessException(ErrorCode.PLAN_CONSTRAINT,
                                    String.format("试卷创建日期[%s]小于[2017-12-01 00:00:00]", paper.getCreated_at()));
                        if (!paper.getStatus_text().contains("已发布"))
                            throw new BusinessException(ErrorCode.PLAN_CONSTRAINT,
                                    String.format("试卷状态[%s]不正确,必须是[已发布]", paper.getStatus_text()));
                        if (!paper.getPaper_types_text().contains("模考"))
                            throw new BusinessException(ErrorCode.PLAN_CONSTRAINT,
                                    String.format("试卷类型[%s]不正确,必须是[模考]", paper.getPaper_types_text()));
                    }
                }

                // step 2 : 调用题库侧校验接口校验
                ExamPaperClient.CheckResponse checkResponse = examPaperClient.check(plan);
                if (!checkResponse.isSuccess())
                    throw new BusinessException(ErrorCode.PLAN_CONSTRAINT, checkResponse.getErrorMessage());
            }

        }
    }

    @Override
    public int countExamStudent(Long planId) {
        ExamPlanEntity planEntity = examPlanDao.findById(planId);
        final String examId = planEntity.getExamId();
        if (StringUtils.isBlank(examId))
            throw new BusinessException(ErrorCode.EXAM_COUNT_STUDENT,
                    String.format("测评[id=%s]没有考试id", planId));
        return examClient.countExamStudent(examId);
    }

    @Override
    public ReportExistDto queryExistInfo(ExamReportQueryParams params){
        ExamReportClient.ReportResponse reportResponse = examReportClient.queryReportExistInfo(params);
        ReportExistDto reportExistDto = ReportExistDto.build(reportResponse);
        return reportExistDto;
    }

    @Override
    public void check(ExamPlan model) {
        //关联的试卷为空
        if (Objects.isNull(model.getPapers())) {
            throw new BusinessException(ErrorCode.EXAM_CREATE_PAPER_NULL);
        }
        //使用的是已有试卷
        if (Objects.equals(model.getPaperType(), PaperType.OLD)) {
            //小学英语标识
            Boolean isEnglish = Objects.equals(model.getSubject(), ExamPlanEnums.Subject.ENGLISH);
            List<String> paperIds = Optional.ofNullable(model.getPapers()).orElse(new ArrayList<>())
                    .stream()
                    .filter(paper -> Objects.nonNull(paper.getPaperId())).map(ExamPlan.Paper::getPaperId)
                    .collect(Collectors.toList());

            List<ExamPaper> papars = paperIds.stream().map(paperid -> {
                //对所属材料校验
                ExamPaperQueryParams params = new ExamPaperQueryParams();
                params.setPaperId(paperid);
                params.setSubject(model.getSubject().subject.name());
                return examPaperDomain.queryOne(params).getData();
            }).collect(Collectors.toList());

            //校验所有试卷总分是否一致
            Boolean allTotalScoreSame = papars.stream().allMatch(papar -> Objects.equals(papars.get(0).getTotalScore(), papar.getTotalScore()));
            if (!allTotalScoreSame) {
                StringBuilder sb = new StringBuilder();
                papars.forEach(papar -> {
                    sb.append(papar.getTotalScore()).append(",");
                });
                sb.deleteCharAt(sb.length() - 1);
                throw new BusinessException(ErrorCode.EXAM_CREATE_TOTALSCORE_INVALID, String.format(ErrorCode.EXAM_CREATE_TOTALSCORE_INVALID.desc, sb.toString()));
            }

            papars.forEach(papar -> {
                //如果是小学英语校验模块类型
                if (isEnglish) {
                    if (model.getType().contains(ExamPlanEnums.Type.SPOKEN)) {
                        if (!Optional.ofNullable(ExamPaperEnums.PartType.mapper(papar.getPartTypes())).orElse(new ArrayList<>()).contains(ExamPlanEnums.Type.SPOKEN)) {//如果测评类型不包含试卷的模块类型，校验失败
                            throw new BusinessException(ErrorCode.EXAM_CREATE_PARTTYPE_INVALID);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void checkBook(ExamPlan model) {
        this.check(model);
        List<String> paperIds = Optional.ofNullable(model.getPapers()).orElse(new ArrayList<>())
                .stream()
                .filter(paper -> Objects.nonNull(paper.getPaperId())).map(ExamPlan.Paper::getPaperId)
                .collect(Collectors.toList());

        List<ExamPaper> papers = paperIds
                .stream()
                .map(paperid -> {
                    ExamPaperQueryParams params = new ExamPaperQueryParams();
                    params.setPaperId(paperid);
                    params.setSubject(model.getSubject().subject.name());
                    return examPaperDomain.queryOne(params).getData();
                }).collect(Collectors.toList());

        StringBuilder uncheckBook = new StringBuilder();
        papers.forEach(paper -> {
            //对所属材料校验
            if (Objects.isNull(paper.getBookIds()) || !paper.getBookIds().contains(model.getBook().getId())) {
                uncheckBook.append(paper.getPaperId()).append(paper.getBookName()).append(";");
            }
        });
        if (StringUtils.isNotBlank(uncheckBook.toString())) {
            throw new BusinessException(ErrorCode.EXAM_CREATE_BOOK_INVALID,
                    String.format("测评的使用教材为“%s”" + "，被关联试卷的教材为“%s”，教材版本不一致，是否继续提交？",
                            model.getBook().getName(), uncheckBook.toString()));
        }
    }

    /**
     * 通知管理
     */
    @Service
    public static class NotifyManager {

        @Resource
        NotifyFactory notifyFactory;

        @Resource
        ExamNotifyDomain notifyDomain;

        @Resource
        ExamPlanOperateLogDao planOperateLogDao;

        /**
         * 通知
         *
         * @param prevEntity    之前
         * @param currentEntity 之后
         */
        void notify(ExamPlanEntity prevEntity, ExamPlanEntity currentEntity) {
            if(currentEntity == null){
                return;
            }
            try {
                ExamPlanEnums.Status prevStatus = prevEntity == null ? null : ExamPlanEnums.Status.valueOf(prevEntity.getStatus());
                ExamPlanEnums.Status currentStatus = ExamPlanEnums.Status.valueOf(currentEntity.getStatus());
                if (PLAN_REJECT != prevStatus && PLAN_REJECT == currentStatus) {
                    notify(currentEntity, getNote(currentEntity.getId()));
                } else if (ExamPlanEnums.Status.PAPER_CHECKING != prevStatus && ExamPlanEnums.Status.PAPER_CHECKING == currentStatus) {
                    notify(currentEntity, "");
                } else if (ExamPlanEnums.Status.PAPER_REJECT != prevStatus && ExamPlanEnums.Status.PAPER_REJECT == currentStatus) {
                    notify(currentEntity, getNote(currentEntity.getId()));
                } else if (ExamPlanEnums.Status.PAPER_PROCESSING != prevStatus && ExamPlanEnums.Status.PAPER_PROCESSING == currentStatus) {
                    notify(currentEntity, "");
                } else if (ExamPlanEnums.Status.PAPER_READY != prevStatus && ExamPlanEnums.Status.PAPER_READY == currentStatus) {
                    notify(currentEntity, "");
                } else if (ExamPlanEnums.Status.EXAM_PUBLISHED != prevStatus && ExamPlanEnums.Status.EXAM_PUBLISHED == currentStatus) {
                    notify(currentEntity, "");
                } else if (ExamPlanEnums.Status.EXAM_OFFLINE != prevStatus && ExamPlanEnums.Status.EXAM_OFFLINE == currentStatus) {
                    notify(currentEntity, "");
                }
            } catch (Exception e) {
                log.error("通知发生错误", e);
            }
        }

        // 获取最近一次操作日志的备注
        private String getNote(Long planId){
            List<ExamPlanOperateLogEntity> operationLogList = planOperateLogDao.findByPlanId(planId);
            if(CollectionUtils.isEmpty(operationLogList)){
                return "";
            }
            return SafeConverter.toString(operationLogList.get(0).getNote(), "");
        }

        /**
         * 通知
         *
         * @param plan 测评计划
         */
        private void notify(ExamPlanEntity plan, String note) {
            ExamNotifyDomain.Request request = notifyFactory.build(plan, note);
            Status status = Status.valueOf(plan.getStatus());
            if(status != Status.EXAM_PUBLISHED && status != Status.EXAM_OFFLINE){
                request.setTypes(Arrays.asList(
                        ExamNotifyDomain.Request.Type.EMAIL,
                        ExamNotifyDomain.Request.Type.SYSTEM,
                        ExamNotifyDomain.Request.Type.PUSH));
            }else {
                request.setTypes(Arrays.asList(
                        ExamNotifyDomain.Request.Type.EMAIL,
                        ExamNotifyDomain.Request.Type.SYSTEM));
            }
            notifyDomain.send(request);
        }

        @Service
        public static class NotifyFactory {

            /**
             * 构建一个通知请求
             *
             * @param planEntity 实体
             * @return 请求
             */
            public ExamNotifyDomain.Request build(ExamPlanEntity planEntity, String note) {
                ExamNotifyDomain.Request request = new ExamNotifyDomain.Request();
                Status status = Status.valueOf(planEntity.getStatus());
                switch (status) {
                    case PLAN_REJECT: {
                        request.setTitle("测评被驳回通知");
                        request.setMessage(String.format("测评ID：%s 测评名称：%s 测评学科：%s 备注：%s",
                                planEntity.getId(),
                                planEntity.getName(),
                                ExamPlanEnums.Subject.valueOf(planEntity.getSubject()).desc,
                                note));
                        request.setReceiver(Arrays.asList(planEntity.getCreatorId()));
                        break;
                    }
                    case PAPER_CHECKING: {
                        request.setTitle("试卷正在审核通知");
                        request.setMessage(String.format("测评ID：%s 测评名称：%s 测评学科：%s",
                                planEntity.getId(),
                                planEntity.getName(),
                                ExamPlanEnums.Subject.valueOf(planEntity.getSubject()).desc));
                        request.setReceiver(Arrays.asList(planEntity.getCreatorId()));
                        break;
                    }
                    case PAPER_REJECT: {
                        request.setTitle("试卷被驳回通知");
                        request.setMessage(String.format("测评ID：%s 测评名称：%s 测评学科：%s 备注：%s",
                                planEntity.getId(),
                                planEntity.getName(),
                                ExamPlanEnums.Subject.valueOf(planEntity.getSubject()).desc,
                                note));
                        request.setReceiver(Arrays.asList(planEntity.getCreatorId(), 1809L, 2230L));
                        break;
                    }
                    case PAPER_PROCESSING: {
                        request.setTitle("试卷录入中通知");
                        request.setMessage(String.format("测评ID：%s 测评名称：%s 测评学科：%s",
                                planEntity.getId(),
                                planEntity.getName(),
                                ExamPlanEnums.Subject.valueOf(planEntity.getSubject()).desc));
                        request.setReceiver(Arrays.asList(planEntity.getCreatorId(), 1809L, 2230L));
                        break;
                    }
                    case PAPER_READY: {
                        request.setTitle("试卷录入完成通知");
                        request.setMessage(String.format("测评ID：%s 测评名称：%s 测评学科：%s",
                                planEntity.getId(),
                                planEntity.getName(),
                                ExamPlanEnums.Subject.valueOf(planEntity.getSubject()).desc));
                        request.setReceiver(Arrays.asList(planEntity.getCreatorId(), 1809L, 2230L));
                        break;
                    }
                    case EXAM_PUBLISHED: {
                        request.setTitle("测评上线通知");
                        request.setMessage(String.format("测评ID：%s 测评名称：%s 测评学科：%s",
                                planEntity.getId(),
                                planEntity.getName(),
                                ExamPlanEnums.Subject.valueOf(planEntity.getSubject()).desc));
                        request.setReceiver(Arrays.asList(planEntity.getCreatorId(), 1809L, 2230L));
                        break;
                    }
                    case EXAM_OFFLINE: {
                        request.setTitle("测评下线通知");
                        request.setMessage(String.format("测评ID：%s 测评名称：%s 测评学科：%s",
                                planEntity.getId(),
                                planEntity.getName(),
                                ExamPlanEnums.Subject.valueOf(planEntity.getSubject()).desc));
                        request.setReceiver(Arrays.asList(planEntity.getCreatorId()));
                        break;
                    }
                }

                return request;
            }
        }
    }
}
