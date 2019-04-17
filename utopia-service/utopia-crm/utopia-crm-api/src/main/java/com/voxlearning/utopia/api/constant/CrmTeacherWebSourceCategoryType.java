package com.voxlearning.utopia.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * Created by Alex on 2015/7/13.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CrmTeacherWebSourceCategoryType {
    SELF_REG("SELF_REG", "自主注册"),
    SELF_REF("SELF_REF", "自主注册"),
    INVITE_REG("INVITE_REG", "邀请注册"),
    BATCH_REG("BATCH_REG", "批量注册"),
    APP_REG("APP_REG", "第三方注册"),
    SELF_APP_REG("SELF_APP_REG", "APP自主注册"),
    OTHER_REG("OTHER_REG", "其他"),
    AFFAIR_TEACHER("AFFAIR_BATCH", "教务老师创建"),

    WECHAT("WECHAT","微信"),
    TEACHER_APP("TEACHER_APP","老师APP"),
    AFFAIR_BATCH("AFFAIR_BATCH","教务注册"),
    PC_SITE("PC_SITE","PC主站"),
    QRCODE_O2O("QRCODE_O2O","O2O扫码"),
    LIVE("LIVE","直播"),
    KLX("KLX","快乐学"),
    ACTIVITY("ACTIVITY","活动"),
    SHEN_SZ("SHEN_SZ","极算"),

    OTHERREG("OTHERREG", "其他"); // TODO

    @Getter public final String name;
    @Getter public final String desc;

    public static Map<String, CrmTeacherWebSourceCategoryType> toMap(){
        Map<String, CrmTeacherWebSourceCategoryType> map = new LinkedHashMap<>();
        for (CrmTeacherWebSourceCategoryType type : values()) {
            map.put(type.name(), type);
        }
        return map;
    }

    public static CrmTeacherWebSourceCategoryType get(String statusName) {
        return toMap().get(statusName);
    }

    public static String getCategoryDesc(String typeName) {
        CrmTeacherWebSourceCategoryType type = get(typeName);
        return type == null ? "--" : type.getDesc();
    }
}

