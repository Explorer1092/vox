package com.voxlearning.utopia.agent.mockexam.dao;

import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperProcessEntity;

/**
 * 试卷流程持久层
 *
 * @author xiaolei.li
 * @version 2018/8/7
 */
public interface ExamPaperProcessDao {

    /**
     * 新建
     *
     * @param entity 数据模型
     */
    void insert(ExamPaperProcessEntity entity);

    /**
     * 修改
     *
     * @param entity 数据模型
     */
    void update(ExamPaperProcessEntity entity);

    /**
     * 根据流程id查询
     *
     * @param processId 流程id
     * @return 数据模型
     */
    ExamPaperProcessEntity findByProcessId(String processId);
}
