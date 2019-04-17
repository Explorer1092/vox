package com.voxlearning.utopia.service.dubbing.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @Author: wei.jiang
 * @Date: Created on 2017/10/31
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DubbingPublishMessageType {
    Homework("作业"),
    UNKNOWN("未知");

    @Getter
    private final String desc;


    public static DubbingPublishMessageType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return UNKNOWN;
        }
    }
}
