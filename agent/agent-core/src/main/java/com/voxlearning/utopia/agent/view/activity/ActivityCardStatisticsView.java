package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

@Data
public class ActivityCardStatisticsView {

    private Long id;
    private Integer idType;
    private String name;

    private Integer dayCardCount;                  // 指定日期送卡数量
    private Integer totalCardCount;                // 累计送卡数量

    private Integer dayUsedCount;                  // 指定日期开卡数量
    private Integer totalUsedCount;                // 累计开卡数量
}
