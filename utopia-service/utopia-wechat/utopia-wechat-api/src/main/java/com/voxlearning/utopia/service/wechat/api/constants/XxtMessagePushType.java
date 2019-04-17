package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by tanguohong on 14-10-29.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum XxtMessagePushType {
    WECHAT_AND_JZT(1, "微信和家长通"),
    WECHAT(2, "微信"),
    JZT(3, "家长通"),
    MOBILE(3, "手机");

    private final int value;
    private final String description;
}
