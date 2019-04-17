package com.voxlearning.utopia.agent.mockexam.domain;

import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaperProcess;

/**
 * @author xiaolei.li
 * @version 2018/8/10
 */
public interface ExamPaperProcessDomain {

    /**
     * 创建
     *
     * @param process 试卷流程
     */
    void create(ExamPaperProcess process);
}
