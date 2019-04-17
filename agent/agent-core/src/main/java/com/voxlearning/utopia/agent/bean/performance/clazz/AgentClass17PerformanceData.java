package com.voxlearning.utopia.agent.bean.performance.clazz;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentClass17PerformanceData
 *
 * @author song.wang
 * @date 2018/1/31
 */
@Getter
@Setter
public class AgentClass17PerformanceData implements Serializable {
    private Long classId;
    private Integer day;
    private AgentClass17PerformanceIndicator indicatorData;
}
