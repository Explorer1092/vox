package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

@Data
public class ActivityOrderStatisticsView {

    private Long id;
    private Integer idType;
    private String name;

    private Integer dayOrderCount;                  // 指定日期订单数量
    private Integer totalOrderCount;                // 累计订单数量
}
