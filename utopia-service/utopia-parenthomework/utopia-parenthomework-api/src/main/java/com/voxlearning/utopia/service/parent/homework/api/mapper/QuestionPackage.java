package com.voxlearning.utopia.service.parent.homework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QuestionPackage implements Serializable {
    /**
     * 题包Id
     */
    private String id;

    /**
     * 题包名称
     */
    private String name;

    /**
     * 题包时长（单位：秒）
     */
    private Long duration;

    /**
     * 试题列表docIds
     */
    private List<String> docIds;

    /**
     * 试题列表docIds
     */
    private List<String> questonIds;

    /**
     * 业务id
     */
    private String bizType;
    /**
     * 作业形式
     */
    private String objectiveConfigType;
    /**
     * 教材id
     */
    private String bookId;

    /**
     * 单元id
     */
    private String unitId;

    /**
     * 课时id
     */
    private String section;

    /**
     * 其他的值
     */
    private Map<String, Object> data;

}
