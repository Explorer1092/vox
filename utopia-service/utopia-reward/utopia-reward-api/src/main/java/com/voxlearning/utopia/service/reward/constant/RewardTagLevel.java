package com.voxlearning.utopia.service.reward.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by XiaoPeng.Yang on 14-7-14.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RewardTagLevel {
    ONE_LEVEL("一级标签"),
    TWO_LEVEL("二级标签");

    @Getter
    private final String description;
}