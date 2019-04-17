package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by jiang wei on 2017/4/5.
 */
@Deprecated
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextBookSourceType {

    SELF_DEVELOP("自研"),
    @Deprecated
    NAMI("纳米盒"),
    UNKNOWN("");


    private final String desc;

    public static TextBookSourceType parse(String name) {
        TextBookSourceType type;
        try {
            type = TextBookSourceType.valueOf(name);
        } catch (Exception e) {
            return TextBookSourceType.UNKNOWN;
        }
        return type;
    }
}
