package com.voxlearning.utopia.agent.mockexam.service.support;

import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.com.alibaba.dubbo.common.json.JSON;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPaperDao;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPlanDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperEntity;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import com.voxlearning.utopia.agent.mockexam.domain.ExamPaperDomain;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaper;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaperProcessState;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlan;
import com.voxlearning.utopia.agent.mockexam.service.ExamPaperService;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperOpenOptionParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperProcessStateNotify;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperOpenOptionResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.*;

/**
 * 试卷服务实现
 *
 * @author xiaolei.li
 * @version 2018/8/6
 */
@Service
public class ExamPaperServiceImpl implements ExamPaperService {

    @Resource
    ExamPaperDomain examPaperDomain;

    @Resource
    ExamPaperDao examPaperDao;
    @Resource
    ExamPlanDao examPlanDao;

    @Override
    public Result<Boolean> handleProcessNotify(ExamPaperProcessStateNotify notify) {
        ExamPaperProcessState model = ExamPaperProcessState.Builder.build(notify);
        examPaperDomain.handleNotify(model);
        return Result.success(true);
    }

    @Override
    public PageResult<ExamPaperDto> queryPage(ExamPaperQueryParams params, PageInfo pageInfo) {
        PageResult<ExamPaperDto> result;
        try {
            PageResult<ExamPaper> pageResult = examPaperDomain.queryPage(params, pageInfo);
            ArrayList<ExamPaper> models = pageResult.getData();
            List<ExamPaperDto> dtos = ExamPaper.Builder.build(models);
            result = PageResult.success((ArrayList) dtos, pageInfo, pageResult.getTotalSize());
        } catch (BusinessException e) {
            result = new PageResult<>();
            result.setSuccess(false);
            ErrorCode errorCode = e.getErrorCode();
            result.setErrorCode(errorCode.code);
            result.setErrorMessage(errorCode.desc);
        } catch (Exception e) {
            result = new PageResult<>();
            result.setSuccess(false);
            ErrorCode errorCode = ErrorCode.UNKNOWN;
            result.setErrorCode(errorCode.code);
            result.setErrorMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public void initPlanForm() {

        List<ExamPaperEntity> allExamPaperEntity = examPaperDao.queryAll();

        Map<String, ExamPlanEntity> paperFormMap = new HashMap<>();
        examPlanDao.queryAll()
                .stream()
                .filter(plan -> Objects.nonNull(plan.getPapers()))
                .forEach(plan -> {
                    List<ExamPlan.Paper> paperList = Optional.ofNullable(JSONObject.parseArray(plan.getPapers(), ExamPlan.Paper.class)).orElse(new ArrayList<>());
                    paperList
                            .stream()
                            .filter(paper -> Objects.nonNull(paper.getPaperId()))
                            .forEach(paper -> {
                                if (paperFormMap.containsKey(paper.getPaperId())) {
                                    if (plan.getCreateDatetime().before(paperFormMap.get(paper.getPaperId()).getCreateDatetime())) {
                                        paperFormMap.put(paper.getPaperId(), plan);
                                    }
                                } else {
                                    paperFormMap.put(paper.getPaperId(), plan);
                                }
                            });
                });

        allExamPaperEntity.stream()
                .filter(paper -> Objects.isNull(paper.getPlanForm()))
                .filter(paper -> paperFormMap.containsKey(paper.getPaperId())).forEach(paper -> {
                    paper.setPlanForm(paperFormMap.get(paper.getPaperId()).getForm());
                    examPaperDao.update(paper);
                });
    }

    @Override
    public Result<ExamPaperOpenOptionResult> openOrClose(ExamPaperOpenOptionParams params) {
        ExamPaperOpenOptionResult result = examPaperDomain.openOrClose(params);
        return Result.success(result);
    }
}
