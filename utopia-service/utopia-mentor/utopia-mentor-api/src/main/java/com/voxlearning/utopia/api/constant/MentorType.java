package com.voxlearning.utopia.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by xiaopeng.yang on 2015/5/28.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MentorType {
    UNKNOWN(0, "未知类型"),
    MENTOR_INITIATIVE(1, "导师主动"),
    MENTEE_INITIATIVE(2, "被帮助人主动");

    @Getter private final int type;  //类型
    @Getter private final String description; //描述
}

