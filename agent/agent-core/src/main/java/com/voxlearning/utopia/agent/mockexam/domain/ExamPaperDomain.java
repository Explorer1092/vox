package com.voxlearning.utopia.agent.mockexam.domain;

import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaper;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaperProcessState;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperOpenOptionParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperOpenOptionResult;

/**
 * 试卷领域服务
 *
 * @author xiaolei.li
 * @version 2018/8/6
 */
public interface ExamPaperDomain {

    /**
     * 处理流程状态通知
     *
     * @param state 流程状态
     */
    void handleNotify(ExamPaperProcessState state);

    /**
     * 录入试卷
     *
     * @param plan 计划
     */
    void create(ExamPlanEntity plan);

    /**
     * 分页查询
     *
     * @param params   查询参数
     * @param pageInfo 分页参数
     * @return 一页数据
     */
    PageResult<ExamPaper> queryPage(ExamPaperQueryParams params, PageInfo pageInfo);

    /**
     * 查找一个
     * @param params
     * @return
     */
    Result<ExamPaper> queryOne(ExamPaperQueryParams params);

    /**
     * 根据试卷id查询试卷
     *
     * @param paperId 试卷id
     * @return 试卷
     */
    ExamPaper retrieve(String paperId);

    /**
     * 开发或者关闭
     *
     * @param params 参数
     * @return 结果
     */
    ExamPaperOpenOptionResult openOrClose(ExamPaperOpenOptionParams params);

    /**
     * 查询总数量
     *
     * @param params 参数
     * @return 总数
     */
    long count(ExamPaperQueryParams params);

    /**
     * 增加引用计数
     *
     * @param paperId 试卷id
     */
    void increaseReference(String paperId, String planForm, String subject);

    /**
     * 引用计数变更
     *
     * @param paperId 试卷id
     */
    void decreaseReference(String paperId,String subject);
}
