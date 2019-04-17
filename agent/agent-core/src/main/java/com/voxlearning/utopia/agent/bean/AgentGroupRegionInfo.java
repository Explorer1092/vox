package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yaguang.wang on 2016/6/21.
 */
@Getter
@Setter
public class AgentGroupRegionInfo implements Serializable{
    private static final long serialVersionUID = -5683997811061580888L;
    private String provinceName;// 省名称
    private String cityName;    // 城市名
    private String countyName;  // 地区名
    private Boolean hasJunior;  // 有小学的权限
    private Boolean hasMiddle;  // 有中学的权限
    private String cityLevel;   //城市等级
}
