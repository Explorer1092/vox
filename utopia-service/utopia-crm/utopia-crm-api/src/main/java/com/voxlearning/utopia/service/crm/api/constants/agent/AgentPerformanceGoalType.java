package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 天玑业绩目标类型
 *
 * @author chunlin.yu
 * @create 2017-10-26 11:57
 **/
@Getter
@RequiredArgsConstructor
public enum AgentPerformanceGoalType {

    COUNTRY("全国"),
    REGION_GROUP("大区"),
    SUB_REGION_GROUP("分区"),
    USER("专员");

    private final String desc;


    private static final Map<String, AgentPerformanceGoalType> enumMap;

    static {
        enumMap = new HashMap<>();
        for (AgentPerformanceGoalType type : AgentPerformanceGoalType.values()) {
            enumMap.put(type.getDesc(), type);
        }
    }

    public static AgentPerformanceGoalType of(String desc){
        try {
            return enumMap.get(desc);
        }catch (Exception e){
            return null;
        }
    }
}
