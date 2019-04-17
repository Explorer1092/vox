package com.voxlearning.utopia.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by XiaoPeng.Yang on 15-5-18.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MentorCategory {
    UNKNOWN(0, "未知类型"),
    MENTOR_AUTHENTICATION(1, "帮助老师认证"),
    MENTOR_NEW_ST_COUNT(2, "帮助新认证老师增加使用学生数"),
    @Deprecated
    MENTOR_TERM_END(3, "期末回馈计划导师活动");

    @Getter private final int type;  //类型
    @Getter private final String description; //描述

    public static MentorCategory of(String type) {
        try {
            return MentorCategory.valueOf(type);
        } catch (Exception ignored) {
            return UNKNOWN;
        }
    }
}
