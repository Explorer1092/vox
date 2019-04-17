package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import lombok.Data;

import java.io.Serializable;

/**
 * 成绩查询参数
 *
 * @Author: peng.zhang
 * @Date: 2018/8/13 17:02
 */
@Data
public class ExamScoreQueryParams implements Serializable {

    /**
     * 计划id
     */
    private Long id;

    /**
     * 考试id
     */
    private String examId;

    /**
     * 学生ID
     */
    private Long studentId;

}
