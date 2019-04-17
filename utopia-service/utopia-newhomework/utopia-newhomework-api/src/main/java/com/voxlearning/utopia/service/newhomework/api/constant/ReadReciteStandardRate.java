package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhangbin
 * @since 2018/1/18
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ReadReciteStandardRate {

    TWENTY_PERCENT(20D, "20%达标"),
    FORTY_PERCENT(40D, "40%达标"),
    SIXTY_PERCENT(60D, "60%达标"),
    EIGHTY_PERCENT(80D, "80%达标"),
    ONE_HUNDRED_PERCENT(100D, "100%达标");

    @Getter
    private final Double precision;
    @Getter
    private final String desc;
}
