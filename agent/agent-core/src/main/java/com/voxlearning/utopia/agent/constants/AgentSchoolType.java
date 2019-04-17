package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学校线索中的学校类型
 * Created by Administrator on 2016/8/23.
 */
public enum AgentSchoolType {
    PUBLIC(1, "公立学校"),
    PRIVATE(3, "私立学校");

    @Getter
    private final int type;
    @Getter
    private final String description;

    AgentSchoolType(int type, String description) {
        this.type = type;
        this.description = description;
    }

    private static final Map<Integer, AgentSchoolType> schoolTypes;

    static {
        schoolTypes = Arrays.asList(values()).stream()
                .collect(Collectors.toMap(AgentSchoolType::getType, t -> t));
    }
    public static AgentSchoolType codeOf(Integer code) {
        return schoolTypes.get(code);
    }

    public static AgentSchoolType safeParse(Integer value) {
        return safeParse(value, PUBLIC);
    }

    public static AgentSchoolType safeParse(Integer value, AgentSchoolType defaultValue) {
        if (value == null) return defaultValue;
        return schoolTypes.getOrDefault(value, defaultValue);
    }
}
