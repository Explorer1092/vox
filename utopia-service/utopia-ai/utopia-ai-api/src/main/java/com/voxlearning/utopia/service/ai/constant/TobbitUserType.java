package com.voxlearning.utopia.service.ai.constant;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum  TobbitUserType {

    MATH("口算"),UNKNOWN("未知");


    private String desc;


    public static TobbitUserType of(String str) {
        try {
            return TobbitUserType.valueOf(str);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

}
