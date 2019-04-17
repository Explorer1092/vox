package com.voxlearning.utopia.agent.bean.performance.clazz;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentClassKlxPerformanceData
 *
 * @author song.wang
 * @date 2018/3/15
 */
@Getter
@Setter
public class AgentClassKlxPerformanceData implements Serializable {

    private Long classId;
    private Integer day;
    private AgentClassKlxPerformanceIndicator indicatorData;
}
