package com.voxlearning.utopia.agent.bean.honeycomb;

import lombok.Data;

@Data
public class HoneycombOrderStatisticsData {

    private Long id;
    private Integer idType;
    private String name;

    private Integer totalCount;                // 累计订单

    private Integer targetCount;               // 异业订单数

}
