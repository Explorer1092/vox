package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

/**
 * @author guoqiang.li
 * @since 2016/3/1
 */
public enum HomeworkContentType {
    QUESTION("试题"),
    PACKAGE("精选包"),
    PAPER("试卷"),
    UNKNOWN("未知");

    @Getter
    private final String desc;

    HomeworkContentType(String desc) {
        this.desc = desc;
    }

    public static HomeworkContentType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return UNKNOWN;
        }
    }
}
