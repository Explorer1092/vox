package com.voxlearning.utopia.agent.view;

import com.voxlearning.utopia.agent.constants.PerformanceGroupType;
import lombok.Getter;
import lombok.Setter;

/**
 * AgentPerformanceGroupView
 *
 * @author song.wang
 * @date 2018/3/1
 */
@Getter
@Setter
public class AgentPerformanceGroupView {

    private Integer month;
    private Long userId;
    private String userName;
    private Long groupId;
    private String groupName;
    private PerformanceGroupType performanceGroupType;
}
