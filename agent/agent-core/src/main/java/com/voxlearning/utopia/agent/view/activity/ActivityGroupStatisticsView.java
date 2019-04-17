package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

@Data
public class ActivityGroupStatisticsView {

    private Long id;
    private Integer idType;
    private String name;

    private Integer dayGroupCount;                  // 指定日期组团数量
    private Integer totalGroupCount;                // 累计组团数量

    private Integer dayCompleteGroupCount;          // 指定日期成功组团数量
    private Integer totalCompleteGroupCount;          // 累计成功组团数量

}
