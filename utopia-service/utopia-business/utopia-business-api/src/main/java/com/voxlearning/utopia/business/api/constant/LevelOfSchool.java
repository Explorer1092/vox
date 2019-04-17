package com.voxlearning.utopia.business.api.constant;

import lombok.Getter;

public enum LevelOfSchool {
    A("A", 15, 30, 45),
    B("B", 20, 40, 60),
    C("C", 30, 60, 90),
    D("D", Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
    ;

    @Getter private final String level;         // A, B, C, D
    @Getter private final int rewardLevel1;     // 奖励档1人数
    @Getter private final int rewardLevel2;     // 奖励档2人数
    @Getter private final int rewardLevel3;     // 奖励档3人数

    LevelOfSchool(String level, int rewardLevel1, int rewardLevel2, int rewardLevel3) {
        this.level = level;
        this.rewardLevel1 = rewardLevel1;
        this.rewardLevel2 = rewardLevel2;
        this.rewardLevel3 = rewardLevel3;
    }

    public static LevelOfSchool parse(String level) {
        try {
            return valueOf(level);
        } catch (Exception ignored) {
            return null;
        }
    }
}