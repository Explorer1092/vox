package com.voxlearning.utopia.agent.bean.honeycomb;

import lombok.Data;

@Data
public class HoneycombPartnerStatisticsData {

    private Long id;
    private Integer idType;
    private String name;

    private Integer totalCount;       // 异业签约数
}
