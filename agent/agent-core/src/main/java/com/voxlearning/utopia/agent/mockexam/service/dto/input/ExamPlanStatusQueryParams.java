package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import lombok.Data;

import java.io.Serializable;

/**
 * 测评审核信息查询
 *
 * @Author: peng.zhang
 * @Date: 2018/8/9 16:03
 */
@Data
public class ExamPlanStatusQueryParams implements Serializable {

    private Long planId;
}
