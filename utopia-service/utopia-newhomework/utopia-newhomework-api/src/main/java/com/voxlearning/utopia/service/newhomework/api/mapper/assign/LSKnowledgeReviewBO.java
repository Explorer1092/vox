package com.voxlearning.utopia.service.newhomework.api.mapper.assign;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2017/3/8 18:18
 */
@Setter
@Getter
public class LSKnowledgeReviewBO implements Serializable{
    private static final long serialVersionUID = 3761070945086033032L;
    private Long groupId;   //班组id
    private String groupName;   //班级名称
    private String id;  //题包id
    private String name;    //题包名
    private String knowledgeTypeName;   //听说读写查缺补漏：未达标、未布置
    private List<Map<String, Object>> basicApps;    //听说查缺补漏练习
}
