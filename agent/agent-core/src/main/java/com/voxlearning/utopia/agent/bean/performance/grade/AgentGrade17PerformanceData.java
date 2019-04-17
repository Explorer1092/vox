package com.voxlearning.utopia.agent.bean.performance.grade;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * AgentGrade17PerformanceData
 *
 * @author song.wang
 * @date 2018/1/31
 */

@Getter
@Setter
public class AgentGrade17PerformanceData  implements Serializable {
    private Long schoolId;
    private Integer day;                                                           // 业绩日期
    private Map<ClazzLevel, AgentGrade17PerformanceIndicator> indicatorDataMap;        // 各个年级的指标数据
}
