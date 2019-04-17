package com.voxlearning.utopia.agent.bean.performance.group;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentGroup17PerformanceData
 *
 * @author song.wang
 * @date 2018/1/31
 */
@Getter
@Setter
public class AgentGroup17PerformanceData implements Serializable {
    private Long groupId;
    private Integer day;
    private AgentGroup17PerformanceIndicator indicatorData;
}
