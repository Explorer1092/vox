package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据报表观察粒度枚举
 * Created by yaguang.wang on 2016/10/13.
 */

@Getter
public enum DataReportGradingType {

    COUNTRY(5, ""),
    REGION(1, ""),
    CITY(2, ""),
    COUNTY(3, ""),
    BUSINESS_DEVELOPER(4, "");

    private static final Map<Integer, DataReportGradingType> gradingType;

    static {
        gradingType = new HashMap<>();
        for (DataReportGradingType type : values()) {
            gradingType.put(type.getKey(), type);
        }
    }

    DataReportGradingType(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    private final int key;
    private final String value;
}
