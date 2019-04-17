package com.voxlearning.utopia.service.newhomework.api.mapper.assign;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2017/1/12 10:45
 */
@Setter
@Getter
public class KnowledgePointQuestionBO implements Serializable {
    private static final long serialVersionUID = 5019908396569247132L;
    private Long groupId;   //班组id
    private String groupName;   //班级名称
    private String id;  //题包id
    private String name;    //题包名
    private Integer knowledgePointNum;  //知识点数目
    private Integer questionNum;    //总题数
    private Long seconds;   //总用时长
    private List<Map<String, Object>> questions;   //班组知识点题目集合
    private List<Map<String, Object>> knowledgePointList;  //知识点集合
    private List<Map<String, Object>> knowledgePointTypeList;   //知识点类型详情集合
    private String knowledgeTypeName;   //查缺补漏(听说读写)：未达标、未布置
}

