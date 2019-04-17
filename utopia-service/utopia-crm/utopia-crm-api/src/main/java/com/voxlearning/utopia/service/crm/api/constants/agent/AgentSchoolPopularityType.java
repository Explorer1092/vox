package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * 学校等级
 * Created by yaguang.wang on 2017/2/13.
 */
@Getter
@AllArgsConstructor
public enum AgentSchoolPopularityType {
    A("A", "名校"),
    B("B", "重点校"),
    C("C", "普通校"),
    D("D", "只做英语online作业学校"),
    E("E", "E");

    private final String level;
    private final String describe;
    private final static Map<String, AgentSchoolPopularityType> typeMap = new HashMap<>();

    static {
        for (AgentSchoolPopularityType type : AgentSchoolPopularityType.values()) {
            typeMap.put(type.level, type);
        }
    }

    public static AgentSchoolPopularityType of(String level) {
        return typeMap.get(level);
    }

    public static List<AgentSchoolPopularityType> viewSchoolPopularity() {
        return Arrays.asList(AgentSchoolPopularityType.values());
    }

    public static AgentSchoolPopularityType safeParse(String level) {
        return typeMap.getOrDefault(level, AgentSchoolPopularityType.B);
    }

    public static AgentSchoolPopularityType safeParse(String level, AgentSchoolPopularityType defaultType) {
        return typeMap.getOrDefault(level, defaultType);
    }
}

