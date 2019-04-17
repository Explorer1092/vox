package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016/4/19
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum JxtNoticeType {
    ClAZZ_AFFAIR(1, "通知"),
    OFFLINE_HOMEWORK(2,"作业单"),
    UNKNOWN(100, "未知");

    private final int type;
    private final String name;

    public static final Map<Integer, JxtNoticeType> map;

    static {
        map = new HashMap<>();
        for (JxtNoticeType type : JxtNoticeType.values()) {
            map.put(type.getType(), type);
        }
    }

    public static JxtNoticeType ofWithUnKnow(Integer type) {
        return map.getOrDefault(type, UNKNOWN);
    }


}
