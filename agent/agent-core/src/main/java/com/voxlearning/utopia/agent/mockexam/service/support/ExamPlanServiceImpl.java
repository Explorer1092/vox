package com.voxlearning.utopia.agent.mockexam.service.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPlanDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import com.voxlearning.utopia.agent.mockexam.domain.ExamDomain;
import com.voxlearning.utopia.agent.mockexam.domain.ExamPlanDomain;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlan;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamStudentScore;
import com.voxlearning.utopia.agent.mockexam.domain.support.UserInfoDomain;
import com.voxlearning.utopia.agent.mockexam.service.ExamPlanService;
import com.voxlearning.utopia.agent.mockexam.service.dto.*;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.*;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPlanDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamStudentScoreDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ReportExistDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 考试计划服务实现
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Service
//@ExposeService(interfaceClass = AgentUserLoader.class)
public class ExamPlanServiceImpl implements ExamPlanService {

    @Resource
    ExamPlanDomain examPlanDomain;

    @Resource
    ExamDomain examDomain;

    @Resource
    ExamPlanDao examPlanDao;

    @Resource
    UserInfoDomain userInfoDomain;

    @Override
    public Result<Boolean> create(ExamPlanDto dto) {
        ExamPlan model = ExamPlan.Builder.build(dto);
        examPlanDomain.create(model);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> submit(ExamPlanDto dto) {
        ExamPlan model = ExamPlan.Builder.build(dto);
        examPlanDomain.submit(model);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> submitBookCheck(ExamPlanDto dto) {
        ExamPlan model = ExamPlan.Builder.build(dto);
        examPlanDomain.checkBook(model);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> withdraw(OperateRequest params) {
        examPlanDomain.withdraw(params);
        return Result.success(true);
    }

    @Override
    public Result<ExamPlanDto> retrieve(Long id) {
        ExamPlan model = examPlanDomain.retrieve(id);
        ExamPlanDto dto = ExamPlan.Builder.build(model);
        return Result.success(dto);
    }

    @Override
    public Result<Boolean> update(ExamPlanDto dto) {
        ExamPlan model = ExamPlan.Builder.build(dto);
        examPlanDomain.update(model);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> audit(ExamPlanAuditParams params) {
        examPlanDomain.audit(params);
        return Result.success(true);
    }

    @Override
    public Result<String> batchAudit(BatchExamPlanAuditParams params) {
        if (Objects.isNull(params) || CollectionUtils.isEmpty(params.getIds())) {
            return Result.success("测评id列表为空");
        }

        Result result = Result.success("");
        StringBuilder msgInfo = new StringBuilder();
        AtomicInteger successNum = new AtomicInteger(0);
        params.getIds().forEach(id -> {
            ExamPlanAuditParams p = new ExamPlanAuditParams();
            p.setId(id);
            p.setOption(params.getOption());
            p.setOperatorId(params.getOperatorId());
            p.setOperatorName(params.getOperatorName());
            p.setOperatorRoles(params.getOperatorRoles());
            try {
                examPlanDomain.audit(p);
                successNum.addAndGet(1);
            } catch (BusinessException be) {
                msgInfo.append(String.format("审核失败，测评id: %s", p.getId())).append("\r\n");
            }
        });
        msgInfo.insert(0, String.format("批量审核条数: %s, 成功条数: %s", params.getIds().size(), successNum.get())).append("\r\n");
        return result.success(msgInfo.toString());
    }

    @Override
    public Result<Boolean> online(OperateRequest params) {
        examPlanDomain.online(params);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> offline(OperateRequest params) {
        examPlanDomain.offline(params);
        return Result.success(true);
    }

    @Override
    public Result<HashMap<Long, String>> makeupExam(ExamMakeupParams params) {
        return Result.success((HashMap<Long, String>) examDomain.makeup(params));
    }

    @Override
    public Result<HashMap<Long, String>> replenishExam(ExamReplenishParams params) {
        return Result.success((HashMap<Long, String>) examDomain.replenish(params));
    }

    @Override
    public PageResult<ExamPlanDto> queryPage(ExamPlanQueryParams params, PageInfo pageInfo) {
        PageResult<ExamPlanDto> result;
        try {
            long count = examPlanDomain.count(params);
            List<ExamPlan> models = examPlanDomain.query(params, pageInfo);
            List<ExamPlanDto> dtos = models.stream().map(ExamPlan.Builder::build).collect(Collectors.toList());
            result = PageResult.success((ArrayList) dtos, pageInfo, count);
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
    public List<ExamPlanEntity> queryAll() {
        return examPlanDao.queryAll();
    }

    @Override
    public Result<ExamPlanDto> getDefaultPlan() {
        ExamPlan model = examPlanDomain.getDefaultPlan();
        ExamPlanDto dto = ExamPlan.Builder.build(model);
        return Result.success(dto);
    }

    @Override
    public Result<ExamStudentScoreDto> queryScore(ExamScoreQueryParams params) {
        ExamStudentScore model = examDomain.queryScore(params);
        if (null == model) {
            return Result.error(ErrorCode.EXAM_SCORE, "暂无该学生成绩数据");
        } else {
            return Result.success(ExamStudentScoreDto.Builder.build(model));
        }
    }

    @Override
    public Result<Boolean> uploadPaper(ExamUploadParams params) {
        return examDomain.uploadFile(params);
    }

    @Override
    public Result<ArrayList<String>> queryCreator(String creatorName) {
        List<String> nameList = examPlanDomain.getCreatorName(creatorName);
        return Result.success((ArrayList) nameList);
    }

    @Override
    public Result<ExamPlanDto> queryRegions(Long id) {
        ExamPlan model = examPlanDomain.retrieve(id);
        ExamPlanDto dto = ExamPlan.Builder.build(model);
        return Result.success(dto);
    }

    @Override
    public Result<ReportExistDto> queryReportExistInfo(ExamReportQueryParams params){
        ReportExistDto reportExistDto = examPlanDomain.queryExistInfo(params);
        return Result.success(reportExistDto);
    }

    @Override
    public Result<Integer> countExamStudent(Long planId) {
        return Result.success(examPlanDomain.countExamStudent(planId));
    }
}
