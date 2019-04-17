package com.voxlearning.utopia.agent.mockexam.service;

import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import com.voxlearning.utopia.agent.mockexam.service.dto.OperateRequest;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.*;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPlanDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamStudentScoreDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ReportExistDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 考试计划服务
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
//@Service
//@ServiceVersion(version = "2018.10.01")
//@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
public interface ExamPlanService {

    /**
     * 新建
     *
     * @param dto 数据
     * @return 结果
     */
    Result<Boolean> create(ExamPlanDto dto);

    /**
     * 提交审核
     *
     * @param dto 计划
     * @return 结果
     */
    Result<Boolean> submit(ExamPlanDto dto);

    /**
     * 提交教材检查
     *
     * @param dto 计划
     * @return 结果
     */
    Result<Boolean> submitBookCheck(ExamPlanDto dto);

    /**
     * 撤销
     *
     * @param params 参数
     */
    Result<Boolean> withdraw(OperateRequest params);

    /**
     * 获取详单
     *
     * @param id 主键
     * @return 考试计划
     */
    Result<ExamPlanDto> retrieve(Long id);

    /**
     * 更新
     *
     * @param dto 计划
     * @return 结果
     */
    Result<Boolean> update(ExamPlanDto dto);

    /**
     * 审核
     *
     * @param params 参数
     * @return 结果
     */
    Result<Boolean> audit(ExamPlanAuditParams params);

    /**
     * 批量审核
     * @param params
     * @return
     */
    Result<String> batchAudit(BatchExamPlanAuditParams params);

    /**
     * 考试上线
     *
     * @param params 参数
     * @return 结果
     */
    Result<Boolean> online(OperateRequest params);

    /**
     * 考试上线
     *
     * @param params 参数
     * @return 结果
     */
    Result<Boolean> offline(OperateRequest params);

    /**
     * 补考
     *
     * @param params 参数
     * @return 结果
     */
    Result<HashMap<Long, String>> makeupExam(ExamMakeupParams params);

    /**
     * 重考
     *
     * @param params 参数
     * @return 结果
     */
    Result<HashMap<Long, String>> replenishExam(ExamReplenishParams params);

    /**
     * 分页查询
     *
     * @param params   查询条件
     * @param pageInfo 分页参数
     * @return 一页数据
     */
    PageResult<ExamPlanDto> queryPage(ExamPlanQueryParams params, PageInfo pageInfo);

    /**
     * 查找所有
     * @return
     */
    List<ExamPlanEntity> queryAll();

    /**
     * 新增创建默认值
     *
     * @return 初始测评
     */
    Result<ExamPlanDto> getDefaultPlan();

    /**
     * 成绩查询
     *
     * @param params 参数
     * @return 成绩详情
     */
    Result<ExamStudentScoreDto> queryScore(ExamScoreQueryParams params);

    /**
     * 上传文件
     *
     * @param params 参数
     * @return 是否成功
     */
    Result<Boolean> uploadPaper(ExamUploadParams params);

    /**
     * 按名称模糊查找创建人
     *
     * @param creatorName 创建人姓名
     * @return 创建人
     */
    Result<ArrayList<String>> queryCreator(String creatorName);

    /**
     * 根据测评 ID 查询对应区域
     * @param id
     * @return
     */
    Result<ExamPlanDto> queryRegions(Long id);

    /**
     * 查询
     * @param params
     * @return
     */
    Result<ReportExistDto> queryReportExistInfo(ExamReportQueryParams params);

    /**
     * 查询参考人数
     *
     * @param planId 测评id
     * @return 人数
     */
    Result<Integer> countExamStudent(Long planId);
}
