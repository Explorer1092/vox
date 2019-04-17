package com.voxlearning.utopia.agent.bean.performance.group;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentGroupKlxPerformanceData
 *
 * @author song.wang
 * @date 2018/3/15
 */
@Getter
@Setter
public class AgentGroupKlxPerformanceData implements Serializable {
    private Long groupId;
    private Integer day;
    private AgentGroupKlxPerformanceIndicator indicatorData;
}
