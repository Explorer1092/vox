package com.voxlearning.utopia.agent.mockexam.dao;

import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperProcessStatusEntity;

/**
 * 试卷流程持久层
 *
 * @author xiaolei.li
 * @version 2018/8/7
 */
public interface ExamPaperProcessStateDao {

    /**
     * 新建
     *
     * @param process
     */
    void insert(ExamPaperProcessStatusEntity process);
}
