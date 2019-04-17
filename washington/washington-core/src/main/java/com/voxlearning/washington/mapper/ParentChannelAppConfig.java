package com.voxlearning.washington.mapper;

import lombok.Data;

import java.util.List;

/**
 * Created by jiang wei on 2017/3/14.
 */
@Data
public class ParentChannelAppConfig {

    public ParentChannelAppConfig() {

    }

    private String channelId;  //app所属频道的id
    private String appId;      //appId
    private String appName;    //名称
    private String appRank;    //排序
    private String imgUrl;      //图标
    private String jumpUrl;     //跳转链接
    private String jumpType;    //跳转类型
    private List<String> albumIds;   //专辑id
    private String appType;    //应用类型（H5、Native）
}
