package com.voxlearning.utopia.agent.bean.performance.school;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 快乐学学校业绩字典
 *
 **/
@Getter
@Setter
public class AgentSchoolKlxPerformanceData implements Serializable {

    private static final long serialVersionUID = 1505576128275934132L;

    private Long schoolId;
    private Integer day;                                                           // 业绩日期

    private AgentSchoolKlxPerformanceIndicator indicatorData;                      // 指标数据

}
