package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务枚举类
 * @author deliang.che
 * @date 2018-05-24
 */
@Getter
public enum AgentTaskType {


    INSCHOOL_SERVICE_TEACHER("进校维护老师"),
    ONLINE_SERVICE_TEACHER("线上维护老师");


    public final String value;

    private static Map<String, AgentTaskType> descMap = new HashMap<>();
    static {
        for (AgentTaskType item : AgentTaskType.values()) {
            descMap.put(item.getValue(), item);
        }
    }

    AgentTaskType(String value) {
        this.value = value;
    }

    public static AgentTaskType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static AgentTaskType descOf(String name){
        return descMap.get(name);
    }

}
