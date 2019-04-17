package com.voxlearning.utopia.enums;

import lombok.Getter;

@Getter
public enum ActivityPatternEnum {

    LIMIT_TIME("竞速模式"),
    NORMAL("得分模式");

    ActivityPatternEnum(String name) {
        this.name = name;
    }

    private String name;
}
