package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by shiwei.liao on 2015/7/20.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProvideType {
    TEACHER(1, "老师"),
    RESEARCHSTAFF(2, "教研员"),
    BUREAU(3, "教育局"),
    OTHER(4, "其他");

    public final int typeId;
    public final String desc;

    public static Map<String, ProvideType> toMap() {
        Map<String, ProvideType> map = new LinkedHashMap<>();
        for (ProvideType type : values()) {
            map.put(type.name(), type);
        }
        return map;
    }

    public static ProvideType get(String statusName) {
        return toMap().get(statusName);
    }
}
