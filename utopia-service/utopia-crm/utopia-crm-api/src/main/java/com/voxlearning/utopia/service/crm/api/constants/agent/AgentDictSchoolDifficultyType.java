package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务难度
 * Created by yaguang.wang on 2017/2/13.
 */
@Getter
@AllArgsConstructor
public enum AgentDictSchoolDifficultyType {
    S("S", "任务难度S级");
    private final String level;
    private final String describe;
    private final static Map<String, AgentDictSchoolDifficultyType> typeMap = new HashMap<>();

    static {
        for (AgentDictSchoolDifficultyType type : AgentDictSchoolDifficultyType.values()) {
            typeMap.put(type.level, type);
        }
    }

    public static AgentDictSchoolDifficultyType of(String level) {
        return typeMap.get(level);
    }

    public static List<Map<String, String>> viewSchoolDifficulty() {
        List<Map<String, String>> result = new ArrayList<>();
        for (AgentDictSchoolDifficultyType type : AgentDictSchoolDifficultyType.values()) {
            Map<String, String> info = new HashMap<>();
            info.put("level", type.getLevel());
            info.put("describe", type.getDescribe());
            result.add(info);
        }
        return result;
    }
}
