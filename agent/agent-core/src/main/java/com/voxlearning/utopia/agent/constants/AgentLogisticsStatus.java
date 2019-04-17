package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

/**
 * AgentLogisticsStatus
 *
 * @author song.wang
 * @date 2016/9/7
 */
public enum AgentLogisticsStatus {

    PACKAGING("配货中"),
    DELIVERED("已发货");

    @Getter
    private final String value;

    AgentLogisticsStatus(String value){
        this.value = value;
    }

    public static AgentLogisticsStatus nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }


}
