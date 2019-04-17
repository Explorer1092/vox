package com.voxlearning.utopia.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * Created by Alex on 2015/7/7.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CrmTeacherUsageStatusType {
    NOCLAZZ("NOCLAZZ", "注册未建班"),
    NOUSE("NOUSE", "建班未使用"),
    NOCHECK("NOCHECK", "使用未检查"),
    NOAUTH("NOAUTH", "使用检查-未认证"),
    AUTHED("AUTHED", "已认证");

    @Getter private final String name;
    @Getter private final String desc;

    public static Map<String, CrmTeacherUsageStatusType> toMap(){
        Map<String, CrmTeacherUsageStatusType> map = new LinkedHashMap<>();
        for (CrmTeacherUsageStatusType type : values()) {
            map.put(type.name(), type);
        }
        return map;
    }

    public static CrmTeacherUsageStatusType get(String statusName) {
        return toMap().get(statusName);
    }
}
