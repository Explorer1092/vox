package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

@Data
public class ActivityOrderUserStatisticsView {

    private Long id;
    private Integer idType;
    private String name;

    private Integer dayOrderUserCount;                  // 指定日期首次下该活动订单的用户数量 (重复下单只算一个)
    private Integer totalOrderUserCount;                // 累计下单用户数数量

}
