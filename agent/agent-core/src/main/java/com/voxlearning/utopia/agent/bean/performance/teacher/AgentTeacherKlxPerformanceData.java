package com.voxlearning.utopia.agent.bean.performance.teacher;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentTeacherKlxPerformanceData
 *
 * @author song.wang
 * @date 2018/3/15
 */
@Getter
@Setter
public class AgentTeacherKlxPerformanceData implements Serializable {
    private Long teacherId;
    private Integer day;
    private AgentTeacherKlxPerformanceIndicator indicatorData;
}
