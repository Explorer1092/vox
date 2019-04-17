package com.voxlearning.utopia.business.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TeachingResourceUserType {
    All("所有老师"),
    @Deprecated
    Unauthorized("未认证老师"),
    Authorized("认证老师");
    @Getter
    private final String description;

    public static TeachingResourceUserType safeValueOf(String name) {
        for (TeachingResourceUserType value : TeachingResourceUserType.values()) {
            if (Objects.equals(name, value.name())) {
                return value;
            }
        }
        return All;
    }
}
