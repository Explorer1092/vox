/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 老师异常状态
 *
 * @author Jia HuanYin
 * @since 2015/7/8
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CrmTeacherUnusualStatusType {
    NO_ASSIGN_5DAYS("NO_ASSIGN_5DAYS", "最近5天未布置作业"),
    NOCLS_AFTER_REG_2DAYS("NOCLS_AFTER_REG_2DAYS", "注册2天未建班"),
    NOUSE_AFTER_CLS_3DAYS("NOUSE_AFTER_CLS_3DAYS", "建班3天未使用"),
    NOSTU_LOGIN_AFTER_CLS_4DAYS("NOSTU_LOGIN_AFTER_CLS_4DAYS", "建班4天无学生登录"),
    NOCHECK_AFTER_ASSIGNHW_3DAYS("NOSTUDENT_FNH_HW_REG_3DAYS", "首次布置作业3天未检查"),
    NOAUTH_AFTER_REG_12DAYS("NOAUTH_AFTER_REG_12DAYS", "注册12天未认证");

    @Getter
    public final String name;
    @Getter
    public final String desc;

    public static Map<String, CrmTeacherUnusualStatusType> toMap() {
        Map<String, CrmTeacherUnusualStatusType> map = new LinkedHashMap<>();
        for (CrmTeacherUnusualStatusType type : values()) {
            map.put(type.name(), type);
        }
        return map;
    }

    public static CrmTeacherUnusualStatusType get(String statusName) {
        return toMap().get(statusName);
    }

    public static Map<String, CrmTeacherUnusualStatusType> toValidMap() {
        Map<String, CrmTeacherUnusualStatusType> map = new LinkedHashMap<>();
        map.put(NOCLS_AFTER_REG_2DAYS.getName(), NOCLS_AFTER_REG_2DAYS);
        map.put(NOUSE_AFTER_CLS_3DAYS.getName(), NOUSE_AFTER_CLS_3DAYS);
        map.put(NO_ASSIGN_5DAYS.getName(), NO_ASSIGN_5DAYS);
        return map;
    }

}
