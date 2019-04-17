package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

public enum Correction {
    EXCELLENT("优"), GOOD("良"), FAIR("中"), PASS("差"), FAIL("没通过"), RIGHT("对"), WRONG("错"), UNKNOWN("未知");

    @Getter
    public final String description;

    Correction(String description) {
        this.description = description;
    }

    public static Correction of(String name) {
        try {
            return Correction.valueOf(name);
        } catch (Exception ex) {
            return UNKNOWN;
        }
    }
}