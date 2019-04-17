package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 区域等级
 * @author deliang.che
 * @since 2018/12/10
 */
@Getter
@RequiredArgsConstructor
public enum AgentRegionRank {
    COUNTRY(1, "国家级"),
    PROVINCE(2, "省级"),
    CITY(3, "市级"),
    COUNTY(4, "区级");

    private final int id;
    private final String rankName;

    private static final Map<Integer, AgentRegionRank> regionMap;

    static {
        regionMap = new HashMap<>();
        for (AgentRegionRank rank : AgentRegionRank.values()) {
            regionMap.put(rank.getId(), rank);
        }
    }

    public static AgentRegionRank rankOf(Integer id) {
        if (id == null) {
            return null;
        }
        return regionMap.get(id);
    }
}
