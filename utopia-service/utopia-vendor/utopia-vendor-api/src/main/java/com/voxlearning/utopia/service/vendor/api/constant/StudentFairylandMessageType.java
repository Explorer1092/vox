package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 学生端课外乐园消息类型
 * 对应appMessageSource为FAIRYLAND
 *
 * @author peng.zhang.a
 * @since 16-8-3
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum StudentFairylandMessageType {
    OPEN_APP_MSG(101, "打开应用"),
    COMMON_MSG(102, "普通消息");


    private static Map<Integer, StudentFairylandMessageType> types;

    static {
        types = new LinkedHashMap<>();
        for (StudentFairylandMessageType typeMid : values()) {
            types.put(typeMid.type, typeMid);
        }
    }

    public final int type;
    public final String description;

    public static StudentFairylandMessageType of(Integer type) {
        if (null == type) {
            return null;
        }
        return types.get(type);
    }
}
