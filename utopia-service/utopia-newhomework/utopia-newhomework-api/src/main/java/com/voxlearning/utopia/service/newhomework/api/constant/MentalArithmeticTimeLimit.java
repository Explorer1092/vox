package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhangbin
 * @since 2017/12/26
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MentalArithmeticTimeLimit {

    ZERO(0, "不限时"),
    ONE(1, "1分钟"),
    TWO(2, "2分钟"),
    THREE(3, "3分钟"),
    FOUR(4, "4分钟"),
    FIVE(5, "5分钟"),
    SEVEN(7, "7分钟"),
    TEN(10, "10分钟"),
    FIFTEEN(15, "15分钟");

    @Getter
    private final Integer time;
    @Getter
    private final String desc;

    public static MentalArithmeticTimeLimit of(String name) {
        try {
            return MentalArithmeticTimeLimit.valueOf(name);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static MentalArithmeticTimeLimit of(Integer key) {
        if (key == null) {
            return ZERO;
        }
        for (MentalArithmeticTimeLimit e : values()) {
            if (e.getTime().equals(key)) {
                return e;
            }
        }
        return ZERO;
    }

}
