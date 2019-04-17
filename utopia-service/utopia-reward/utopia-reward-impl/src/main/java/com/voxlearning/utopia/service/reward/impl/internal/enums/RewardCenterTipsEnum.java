package com.voxlearning.utopia.service.reward.impl.internal.enums;

public enum RewardCenterTipsEnum {
    DEFAULT(0x0),
    INTEGRAL_OFFSET_FREIGHT(0x1),
    GRADUATE_STOP_CONVERT(0x2),
    ;
    private int value;
    private RewardCenterTipsEnum(int value) {
        this.value = value;
    }

    public int intValue() {
        return value;
    }
}
