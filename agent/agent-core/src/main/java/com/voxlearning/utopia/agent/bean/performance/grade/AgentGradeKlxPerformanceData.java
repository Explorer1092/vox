package com.voxlearning.utopia.agent.bean.performance.grade;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * AgentGradeKlxPerformanceData
 *
 * @author song.wang
 * @date 2018/3/15
 */
@Getter
@Setter
public class AgentGradeKlxPerformanceData implements Serializable {
    private Long schoolId;
    private Integer day;                                                           // 业绩日期
    private Map<ClazzLevel, AgentGradeKlxPerformanceIndicator> indicatorDataMap;        // 各个年级的指标数据
}
