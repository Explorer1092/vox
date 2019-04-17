package com.voxlearning.utopia.service.reward.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 商品投放类型
 * Created by ganhaitian on 2017/3/23.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RewardProductTargetType {

    TARGET_TYPE_UNKNOWN(-1, "未知类型"),
    TARGET_TYPE_REGION(1, "公众号投放地区"),
    TARGET_TYPE_ALL(5, "投放所有用户");

    @Getter private final int type;
    @Getter private final String desc;

    public static RewardProductTargetType of(Integer type) {
        for (RewardProductTargetType target : values()) {
            if (target.getType() == type) {
                return target;
            }
        }
        return TARGET_TYPE_UNKNOWN;
    }
}
