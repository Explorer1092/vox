package com.voxlearning.utopia.service.campaign.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Xiaochao.Wei
 * @since 2018/1/31
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AwardType {
    INTEGRAL("学豆"),
    HEAD_WEAR("头饰"),
    RC_PRODUCT("奖品中心实物奖品");

    @Getter
    private final String info;
}
