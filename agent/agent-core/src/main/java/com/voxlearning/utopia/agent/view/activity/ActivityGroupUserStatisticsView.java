package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

@Data
public class ActivityGroupUserStatisticsView {

    private Long id;
    private Integer idType;
    private String name;

    private Integer dayUserCount;                  // 指定日期组团数量
    private Integer totalUserCount;                // 累计组团数量

    private Integer dayCompleteUserCount;          // 指定日期成功组团数量
    private Integer totalCompleteUserCount;          // 累计成功组团数量


}
