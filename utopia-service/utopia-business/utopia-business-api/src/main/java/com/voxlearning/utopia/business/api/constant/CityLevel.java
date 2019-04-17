package com.voxlearning.utopia.business.api.constant;

import lombok.Getter;

public enum CityLevel implements IActivityLevel {
    S("S", 25, 50, 75),
    A("A", 25, 50, 75),
    B("B", 30, 60, 90),
    C("C", 30, 60, 90);

    @Getter private final String level;         // S/A/B
    @Getter private final int rewardLevel1;     // 奖励档1人数
    @Getter private final int rewardLevel2;     // 奖励档2人数
    @Getter private final int rewardLevel3;     // 奖励档3人数

    CityLevel(String level, int rewardLevel1, int rewardLevel2, int rewardLevel3) {
        this.level = level;
        this.rewardLevel1 = rewardLevel1;
        this.rewardLevel2 = rewardLevel2;
        this.rewardLevel3 = rewardLevel3;
    }

    @Override
    public int rewardLevel1() {
        return rewardLevel1;
    }

    @Override
    public int rewardLevel2() {
        return rewardLevel2;
    }

    @Override
    public int rewardLevel3() {
        return rewardLevel3;
    }

    @Override
    public String fetchLevel() {
        return level;
    }

    public static CityLevel parse(String level) {
        try {
            return valueOf(level);
        } catch (Exception ignored) {
            return null;
        }
    }
}