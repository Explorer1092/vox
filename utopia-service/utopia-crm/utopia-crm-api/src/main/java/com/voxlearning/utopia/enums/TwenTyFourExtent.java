package com.voxlearning.utopia.enums;

import lombok.Getter;

@Getter
public enum TwenTyFourExtent {
    ONE_TO_TEN("1-10"),
    ONE_TO_THIRTEEN("1-13");

    TwenTyFourExtent(String name) {
        this.name = name;
    }

    private String name;
}
