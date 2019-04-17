package com.voxlearning.utopia.agent.mockexam.controller.view;

import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPlanQueryParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计划查询参数
 * @author xiaolei.li
 * @version 2018/8/12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExamPlanPageQueryParams extends ExamPlanQueryParams {

    /**
     * 页大小
     */
    private Integer size;

    /**
     * 第几页，从0开始
     */
    private Integer page;
}
