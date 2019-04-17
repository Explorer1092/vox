package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 竞品使用场景枚举类
 * @author deliang.che
 * @date 2018/3/8
 */
@Getter
public enum UsageScenarioType {


    ASSIGNMENY("作业"),
    WEEKLY_MEASUREMENT("周测"),
    MONTHLY_EXAMINATION("月考"),
    END_OF_TERM("期中期末"),
    OTHER("其他");


    public final String value;

    private static Map<String, UsageScenarioType> descMap = new HashMap<>();
    static {
        for (UsageScenarioType item : UsageScenarioType.values()) {
            descMap.put(item.getValue(), item);
        }
    }

    UsageScenarioType(String value) {
        this.value = value;
    }

    public static UsageScenarioType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static UsageScenarioType descOf(String name){
        return descMap.get(name);
    }

}
