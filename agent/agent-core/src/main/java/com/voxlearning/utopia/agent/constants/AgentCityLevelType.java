package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * City Level Type
 * Created by Alex on 15-2-12.
 */
@Getter
public enum AgentCityLevelType {

    @Deprecated
    CityLevelSS("S+"),
    CityLevelS("S"),
    CityLevelA("A"),
    CityLevelB("B"),
    CityLevelC("C");


    public final String value;

    private static Map<String, AgentCityLevelType> descMap = new HashMap<>();
    static {
        for (AgentCityLevelType item : AgentCityLevelType.values()) {
            descMap.put(item.getValue(), item);
        }
    }

    AgentCityLevelType(String value) {
        this.value = value;
    }

    public static AgentCityLevelType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static AgentCityLevelType descOf(String name){
        return descMap.get(name);
    }

}
