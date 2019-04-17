package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

@Data
public class ActivityRankingView {

    private Long id;
    private String name;
    private Long groupId;
    private String groupName;

    private Integer ranking;
    private Integer dataValue;
}
