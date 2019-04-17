package com.voxlearning.utopia.service.reward.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class PGRewardMapper implements Serializable{

    private static final long serialVersionUID = -5621693351061362685L;

    private String type;               // 类型
    private Long activityId;           // 活动ID
    private Integer num;               // 数量
    private String name;               // 名称
    private String img;                // ICON
    private Map<String,Object> extAttr;// 自定义属性

}
