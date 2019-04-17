package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author shiwe.liao
 * @since 2016-9-1
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum  ParentHomeworkDynamicToNativeType {
    BOOK_LISTEN(1,"随身听"),
    POINT_READ(2,"点读机"),
    TEXT_READ(3,"语文阅读"),
    ;

    private final int type;
    private final String desc;
}
