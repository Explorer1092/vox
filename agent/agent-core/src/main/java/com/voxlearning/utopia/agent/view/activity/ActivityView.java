package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ActivityView {
    private String id;
    private String name;                       // 活动名称
    private Date startDate;                    // 活动开始时间
    private Date endDate;                      // 活动结束时间
    private String originalPrice;              // 原价
    private String presentPrice;               // 现价
    private List<String> iconUrls;             // 图标URL
    private String linkUrl;                    // 点击活动记录跳转到的URL
}
