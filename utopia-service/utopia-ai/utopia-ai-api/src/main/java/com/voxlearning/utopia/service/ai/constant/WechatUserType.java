package com.voxlearning.utopia.service.ai.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum WechatUserType {
    CHIPS_OFFICIAL_ACCOUNTS(1, "薯条公众号"), CHIPS_MINI_PROGRAM(2, "薯条小程序"), UNKNOWN(0,"未定义");

    @Getter
    private final int code;
    @Getter
    private final String desc;

    public static WechatUserType safeOf(String name) {
        try {
            return WechatUserType.valueOf(name);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
