package com.voxlearning.utopia.agent.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  市场的学校阶段
 * Created by yaguang.wang on 2016/12/6.
 */
@AllArgsConstructor
public enum AgentSchoolLevel {
    JUNIOR(1, "小学"),
    MIDDLE(2, "中学");

    @Getter
    private final int level;
    @Getter
    private final String description;

    private static final Map<Integer, AgentSchoolLevel> schoolLevels;
    static {
        schoolLevels = Arrays.stream(values())
                .collect(Collectors.toMap(AgentSchoolLevel::getLevel, t -> t));
    }

    public static AgentSchoolLevel codeOf(Integer level) {
        return schoolLevels.get(level);
    }

    public static AgentSchoolLevel safeParse(Integer value) {
        return safeParse(value, JUNIOR);
    }

    public static AgentSchoolLevel safeParse(Integer value, AgentSchoolLevel defaultValue) {
        if (value == null) return defaultValue;
        return schoolLevels.getOrDefault(value, defaultValue);
    }
}
