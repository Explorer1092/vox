package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016-9-12
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum HomeworkVendorMessageType {
    OFFLINE_HOMEWORK(1, "作业单"),
    UNKNOWN(100, "未知");

    private final Integer type;
    private final String desc;

    public static final Map<Integer, HomeworkVendorMessageType> map;

    static {
        map = new HashMap<>();
        for (HomeworkVendorMessageType type : HomeworkVendorMessageType.values()) {
            map.put(type.getType(), type);
        }
    }

    public static HomeworkVendorMessageType ofWithUnKnow(Integer type) {
        if (map.containsKey(type)) {
            return map.get(type);
        } else {
            return UNKNOWN;
        }
    }
}
