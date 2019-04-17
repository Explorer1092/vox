package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

/**
 * AgentPerformanceStatisticsType 业绩统计类型
 *
 * @author song.wang
 * @date 2017/3/27
 */
public enum AgentPerformanceStatisticsType {

    USER(1, "用户业绩"),
    GROUP(2, "部门业绩")
    ;

    @Getter
    private final int type;
    @Getter
    private final String desc;

    AgentPerformanceStatisticsType(int type, String desc){
        this.type = type;
        this.desc = desc;
    }


}
