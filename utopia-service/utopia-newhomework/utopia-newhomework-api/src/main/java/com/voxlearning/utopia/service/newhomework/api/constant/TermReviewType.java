package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zhangbin
 * @since 2017/11/7
 */

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TermReviewType {

    BASIC("基础必过", 1),
    FOCUS("重点复习", 2),
    PAPER("模考卷", 3);

    @Getter
    private final String name;
    @Getter
    private final Integer rank;

    public static TermReviewType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ignore) {
            return null;
        }
    }
}
