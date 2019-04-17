package com.voxlearning.washington.mapper;

import lombok.Data;

import java.util.List;

/**
 * Created by jiang wei on 2016/9/19.
 */
@Data
public class ParentTabConfig {

    public ParentTabConfig() {

    }

    private String showId;// 展示标签的位置编号
    private String clickIcon;//选中tab的Icon
    private String notClickIcon;//未选中tab的Icon
    private String tabName;//tab的名称
    private String tabUrl;//tab的url
    private Boolean isDisplay;//该标签是否展示
    private Boolean isDisplayNavigationBar;//该标签是否展示导航条
    private String version;//控制版本
    private Boolean brandFlag;//品牌隔离参数
    private String grayMain;//灰度的main
    private String graySub;//灰度的sub
    private List<Integer> classLevels;//配置的年级(由于灰度里没有对年级的灰度，所以配在这里进行匹配)
    private String displayStartDate; //tab的显示开始时间 时间格式："yyyy-MM-dd HH:mm:ss"
    private String displayEndDate; //tab的显示结束时间 时间格式："yyyy-MM-dd HH:mm:ss"
    private Boolean webViewIsShow;//客户端是否展示webView
    private ParentTabBubble bubble; //运营气泡
    private Boolean wkFlag; //是否使用wk
    private String reminderPosition;//提醒位置
    private List<String> childPositions; //二级提醒位置
}
