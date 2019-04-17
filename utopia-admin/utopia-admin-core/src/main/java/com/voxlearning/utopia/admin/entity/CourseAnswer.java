package com.voxlearning.utopia.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author guangqing
 * @since 2018/8/17
 */
@Data
public class CourseAnswer implements Serializable {
    private List<Map<String, Object>> answerList;
    private int page;
    private Double courseAnswerTimeAvg;
    private int answerSize;
}
