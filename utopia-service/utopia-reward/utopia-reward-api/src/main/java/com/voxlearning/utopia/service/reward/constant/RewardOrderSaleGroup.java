package com.voxlearning.utopia.service.reward.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by XiaoPeng.Yang on 14-7-14.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RewardOrderSaleGroup {
    NORMAL("普通用户"),
    VIP("VIP");

    @Getter
    private final String description;
}
