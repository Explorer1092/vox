package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhangbin
 * @since 2017/12/29
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MentalArithmeticPrecision {

    THIRTY_PERCENT(30D, "30%准确率", 1),
    SEVENTY_PERCENT(70D, "70%准确率", 1),
    ONE_HUNDRED_PERCENT(100D, "100%准确率", 1);

    @Getter
    private final Double precision;
    @Getter
    private final String desc;
    @Getter
    private final Integer award;

}
