package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-12-29 14:32
 **/
@Data
public class LotteryCampsignMapper {
    private Long id;
    private String campaignName;       // 活动名称
    private String campaignStartTime;    // 活动开始时间
    private String campaignEndTime;      // 活动结束时间
    private Boolean onlined;
    private Boolean disabled;
    // 以下是一些抽奖处理相关的通用控制字段
    private Integer bigAwardRewin;     // 重复中大奖控制，// 0：无控制，1：不允许同一个用户中多个大奖，2：不允许同校用户中多个大奖，3：不允许同一个地区用户中多个大奖
    private Long joinCounts;           //参与次数，为空不限
    private Integer joinCountsRange;   //参与次数的限制时间范围，1：每日，2：每周，3：每月
}
