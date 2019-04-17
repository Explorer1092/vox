package com.voxlearning.utopia.service.newhomework.api.mapper.assign;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 期末复习，题包类型格式的公用BO
 *
 * @author zhangbin
 * @since 2017/11/9
 */

@Setter
@Getter
public class TermReviewCommonBO implements Serializable {

    private static final long serialVersionUID = 476667912432146994L;

    // 题包id
    private String id;
    // 题包名
    private String name;
    // 对应的作业形式
    private ObjectiveConfigType objectiveConfigType;
    // 对应的作业形式名称
    private String typeName;
    // 题包是否已布置过
    private Boolean showAssigned;
    // 总题数
    private Integer questionNum;
    // 总用时长
    private Long seconds;
    // 题目集合
    private List<Map<String, Object>> questions;

}
