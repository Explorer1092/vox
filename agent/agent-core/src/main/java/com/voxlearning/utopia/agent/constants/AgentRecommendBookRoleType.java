package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 可被推荐书籍的人角色
 * Created by yaguang.wang on 2016/8/2.
 */
@Getter
@RequiredArgsConstructor
public enum AgentRecommendBookRoleType {

    Region(11, "大区经理"),
    CityManager(13, "市经理"),
    BusinessDeveloper(21, "市场专员");

    private final Integer id;
    private final String roleName;

    public static final Map<Integer, AgentRecommendBookRoleType> roleTypeMap;

    static {
        roleTypeMap = new HashMap<>();
        for (AgentRecommendBookRoleType type : values()) {
            roleTypeMap.put(type.getId(), type);
        }
    }

    public static AgentRecommendBookRoleType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return AgentRecommendBookRoleType.roleTypeMap.get(id);
    }

}
