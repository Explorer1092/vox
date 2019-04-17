package com.voxlearning.washington.mapper;

import lombok.Data;

/**
 * @author malong
 * @since 2018/4/10
 */
@Data
public class ParentTabBubble {
    private String content; //气泡文案
    private String contentAddDate; //文案添加时间，必填，当一次时间范围内文案来回变动时，可以让相同文案再次显示
    private String version;
    private String startDate;   //开始显示时间
    private String endDate; //结束显示时间
    private String grayExpress; //灰度 regionCode_schoolId_clazzLevel_userId
}
