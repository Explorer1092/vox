package com.voxlearning.utopia.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by xiaopeng.yang on 2015/5/28.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MentorLevel {
    AUTHENTICATION_DEFAULT(1, "帮助认证默认类型"),
    MENTOR_NEW_ST_COUNT_LEVEL_ONE(1, "帮助新认证老师增加使用学生数0人-30人"),
    MENTOR_NEW_ST_COUNT_LEVEL_TWO(2, "帮助新认证老师增加使用学生数30人-60人"),
    MENTOR_NEW_ST_COUNT_LEVEL_THREE(3, "帮助新认证老师增加使用学生数60人-90人"),
    MENTOR_TERM_END_LEVEL_ONE(1, "期末回馈计划使用学生数0人-30人"),
    MENTOR_TERM_END_LEVEL_TWO(2, "期末回馈计划使用学生数30人-60人"),
    MENTOR_TERM_END_LEVEL_THREE(3, "期末回馈计划使用学生数60人-90人");

    @Getter private final int level;  //类型
    @Getter private final String description; //描述
}
