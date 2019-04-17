package com.voxlearning.utopia.enums;

import lombok.Getter;

@Getter
public enum ActivityDifficultyLevelEnum {

    EASY(10, "初学者"),
    NORMAL(20, "普通"),
    HARD(30, "困难");

    ActivityDifficultyLevelEnum(long code, String name) {
        this.code = code;
        this.name = name;
    }

    private long code;
    private String name;
}
