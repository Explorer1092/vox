package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author lei.liu
 * @since 2018/7/10
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AssignmentConfigType {

    INTELLIGENCE_EXAM("INTELLIGENCE_EXAM", "同步习题"),;

    @Getter private final String type;
    @Getter private final String name;

    public static AssignmentConfigType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
