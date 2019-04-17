package com.voxlearning.utopia.agent.bean.honeycomb;

import lombok.Data;

@Data
public class HoneycombFansStatisticsData {

    private Long id;
    private Integer idType;
    private String name;

    private Integer totalCount;                // 累计粉丝数
}
