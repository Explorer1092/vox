package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 暂时就有一个  未来想把选角色权限的搞成一个公共的模块 添加的模块再往在理添类型吧
 */
public enum AgentAuthorityType {
    MESSAGE(1, "消息中心"),
    ACTIVITY(2, "市场活动");
    @Getter
    private final int id;
    @Getter
    private final String desc;

    AgentAuthorityType(int id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    private static final Map<Integer, AgentAuthorityType> authorutyMap;

    static {
        authorutyMap = new HashMap<>();
        for (AgentAuthorityType type : AgentAuthorityType.values()) {
            authorutyMap.put(type.getId(), type);
        }
    }

    public static AgentAuthorityType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return authorutyMap.get(id);
    }
}
