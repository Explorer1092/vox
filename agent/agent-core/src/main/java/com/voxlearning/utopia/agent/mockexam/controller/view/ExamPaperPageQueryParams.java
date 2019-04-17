package com.voxlearning.utopia.agent.mockexam.controller.view;

import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperQueryParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 试卷分页查询参数
 *
 * @author xiaolei.li
 * @version 2018/9/5
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExamPaperPageQueryParams extends ExamPaperQueryParams implements Serializable {
    /**
     * 页大小
     */
    private Integer size;

    /**
     * 第几页，从0开始
     */
    private Integer page;
}
