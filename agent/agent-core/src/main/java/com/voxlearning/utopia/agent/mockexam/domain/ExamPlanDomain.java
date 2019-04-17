package com.voxlearning.utopia.agent.mockexam.domain;

import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaperProcessState;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlan;
import com.voxlearning.utopia.agent.mockexam.service.dto.OperateRequest;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPlanAuditParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPlanQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamReportQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ReportExistDto;

import java.util.List;

/**
 * 考试计划领域层接口
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
public interface ExamPlanDomain {

    /**
     * 新建
     *
     * @param model 领域模型
     */
    void create(ExamPlan model);

    /**
     * 更新
     *
     * @param model
     */
    void update(ExamPlan model);

    /**
     * 撤销
     *
     * @param params 参数
     */
    void withdraw(OperateRequest params);

    /**
     * 提交审核
     *
     * @param model 领域模型
     */
    void submit(ExamPlan model);

    /**
     * 测评审核
     *
     * @param params 参数
     */
    void audit(ExamPlanAuditParams params);

    /**
     * 考试上线
     *
     * @param params 参数
     */
    void online(OperateRequest params);

    /**
     * 考试下线
     *
     * @param params 考试计划id
     */
    void offline(OperateRequest params);

    /**
     * 新增测评设置默认值
     *
     * @return
     */
    ExamPlan getDefaultPlan();

    /**
     * 获取详单
     *
     * @param id 主键
     * @return 领域模型
     */
    ExamPlan retrieve(Long id);

    /**
     * 查询总数量
     *
     * @param params 参数
     * @return 总记录数
     */
    long count(ExamPlanQueryParams params);

    /**
     * 分页查询
     *
     * @param params   查询参数
     * @param pageInfo 分页信息
     * @return 一页数据
     */
    List<ExamPlan> query(ExamPlanQueryParams params, PageInfo pageInfo);

    /**
     * 处理试卷的状态影响
     *
     * @param state
     */
    void handlePaperStatus(ExamPaperProcessState state);

    /**
     * 模糊查找测评创建人姓名
     *
     * @param creatorName
     * @return
     */
    List<String> getCreatorName(String creatorName);

    /**
     * 查询参考学生人数
     *
     * @param planId 计划id
     * @return 学生人数
     */
    int countExamStudent(Long planId);

    /**
     * 查询报告是否存在
     * @param params 参数
     * @return 是否存在信息
     */
    ReportExistDto queryExistInfo(ExamReportQueryParams params);

    void check(ExamPlan model);

    void checkBook(ExamPlan model);
}
