package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 省市区
 * Created by yaguang.wang on 2016/10/19.
 */
@Getter
@RequiredArgsConstructor
public enum AgentRegionType {
    PROVINCE(1, "省级"),
    CITY(2, "市级"),
    COUNTY(3, "区级");

    private final int id;
    private final String typeName;

    private static final Map<Integer, AgentRegionType> regionMap;

    static {
        regionMap = new HashMap<>();
        for (AgentRegionType type : AgentRegionType.values()) {
            regionMap.put(type.getId(), type);
        }
    }

    public static AgentRegionType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return regionMap.get(id);
    }
}
