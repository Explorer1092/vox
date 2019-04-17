package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by tanguohong on 14-10-29.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum XxtMessageType {
    ANONYMOUS(0, "未知类型"),
    通知类(1, "通知类"),
    回执类(2, "回执类"),
    礼物问候类(3, "礼物问候类"),
    图片(4, "图片"),
    感恩节(5, "感恩节"),
    作业报告留言(6, "作业报告留言");

    private final int value;
    private final String description;

    private final static Map<Integer, XxtMessageType> xxtMessageTypes;
    static {
        xxtMessageTypes = new LinkedHashMap<>();
        for (XxtMessageType xxtMessageType : values()) {
            xxtMessageTypes.put(xxtMessageType.getValue(), xxtMessageType);
        }
    }

    public static XxtMessageType of(Integer type) {
        if (null == type) {
            return ANONYMOUS;
        }
        return xxtMessageTypes.get(type);
    }
}
