package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 下发通知
 * Created by Administrator on 2016/9/7.
 */
@Getter
@RequiredArgsConstructor
public enum IssuedNotification {
    Country(10, "全国总监"),
    Region(11, "大区经理"),
    CityManager(13, "城市经理"),
    BusinessDeveloper(21, "城市专员"),
    CityAgent(15, "代理");

    private final Integer id;
    private final String roleName;

    public static final Map<Integer, IssuedNotification> roleTypeMap;

    static {
        roleTypeMap = new HashMap<>();
        for (IssuedNotification type : values()) {
            roleTypeMap.put(type.getId(), type);
        }
    }

    public static IssuedNotification typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return IssuedNotification.roleTypeMap.get(id);
    }
}
