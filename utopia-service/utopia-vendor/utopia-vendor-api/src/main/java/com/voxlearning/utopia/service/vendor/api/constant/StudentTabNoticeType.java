package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author malong
 * @since 2016/8/3
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum StudentTabNoticeType {
    RED_DOT(0, "红点", ""),
    TAG(1, "标签", ""),
    NUMBER(2, "数字", "www.17zuoye.com/number.png"),
    UNKNOWN(3, "无", "");

    private final int type;
    private final String desc;
    private final String imgUrl;

    public static StudentTabNoticeType parse(String name) {
        StudentTabNoticeType type;
        try {
            type = StudentTabNoticeType.valueOf(name);
        } catch (Exception e) {
            return StudentTabNoticeType.UNKNOWN;
        }
        return type;
    }
}
