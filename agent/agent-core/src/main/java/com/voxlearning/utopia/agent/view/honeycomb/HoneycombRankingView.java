package com.voxlearning.utopia.agent.view.honeycomb;

import lombok.Data;

@Data
public class HoneycombRankingView {

    private Long id;
    private String name;
    private Long groupId;
    private String groupName;

    private Integer ranking;
    private Integer dataValue;

}
