package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 大区经理的全国271分布
 * Created by yaguang.wang on 2016/7/28.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountryDistributionData implements Serializable {
    private static final long serialVersionUID = 5783205306606272061L;

    private String groupName;               //部门名称
    private Integer top20PerCount;          //前20%的人数
    private List<String> top20PerUserName;  //前20%的用户姓名
    private Integer middle70PerCount;          //中间70%人数
    private List<String> middle70PerUserName;  //中间70%用户的姓名
    private Integer lost10PerCount;         //最后10%的人数
    private List<String> lost10PerUserName; //最后10%的人的姓名
    private Integer cityColour;             //城市颜色   0.黑色 1.红色 2.黄色 3.蓝色
    private Integer orderSize;              //排序编号
}
