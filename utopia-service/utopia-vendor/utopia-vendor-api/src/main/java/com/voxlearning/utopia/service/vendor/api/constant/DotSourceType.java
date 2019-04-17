package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author malong
 * @since 2016/7/20
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DotSourceType {
    HOMEWORK_DYNAMIC_NEW("homework_dynamic_new", "新作业"),
    HOMEWORK_DYNAMIC_FINISH("homework_dynamic_finish", "已完成"),
    HOMEWORK_DYNAMIC_CHECK("homework_dynamic_check", "已检查"); //包括未完成已检查和已完成已检查

    private final String type;
    private final String desc;

    public static DotSourceType nameOf(String name) {
        try {
            return valueOf(name);
        }catch (Exception e){
            return null;
        }
    }
}
