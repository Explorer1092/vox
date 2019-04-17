package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaguang.wang
 * on 2017/3/28.
 */
@Getter
@RequiredArgsConstructor
public enum AgentConfigSchoolLogType {
    ADD(1, "添加"),
    DELETE(2, "删除");

    private final Integer id;
    private final String typeName;
    private static final Map<Integer, AgentConfigSchoolLogType> logMap;

    static {
        logMap = new HashMap<>();
        for (AgentConfigSchoolLogType type : values()) {
            logMap.put(type.getId(), type);
        }
    }

    public static AgentConfigSchoolLogType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return AgentConfigSchoolLogType.logMap.get(id);
    }
}
