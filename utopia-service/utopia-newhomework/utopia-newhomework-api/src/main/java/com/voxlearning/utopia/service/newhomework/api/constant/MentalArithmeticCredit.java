package com.voxlearning.utopia.service.newhomework.api.constant;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MentalArithmeticCredit {

    FIRST(1, "第一名", 12),
    SECOND(2, "第二名", 10),
    THIRD(3, "第三名", 9),
    FORTH(4, "第四名", 8),
    FIFTH(5, "第五名", 7);


    @Getter private final Integer rank; // 名次
    @Getter private final String description;
    @Getter private final Integer credit; //奖励积分

    public static MentalArithmeticCredit of(Integer rank) {
        if (rank == null) {
            return null;
        }
        for (MentalArithmeticCredit t : values()) {
            if (t.getRank() == rank) {
                return t;
            }
        }
        return null;
    }

}


