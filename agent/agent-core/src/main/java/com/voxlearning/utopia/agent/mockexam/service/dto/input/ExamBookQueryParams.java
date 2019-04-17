package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.Grade;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.Subject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 查询教材参数
 *
 * @Author: peng.zhang
 * @Date: 2018/8/14 13:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamBookQueryParams implements Serializable {

    /**
     * 学科
     */
    private Subject subject;

    /**
     * 年级
     */
    private Grade grade;

    /**
     * 关键字匹配
     */
    private String q;
}
