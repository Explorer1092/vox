/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.push.api.constant;

import com.voxlearning.alps.annotation.meta.Ktwelve;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwe.liao
 * @version 0.1
 * @since 2015/12/22
 */
public enum AppMessageSource {
    INFANT(Ktwelve.INFANT, "17Student"),
    STUDENT(Ktwelve.PRIMARY_SCHOOL, "17Student"),
    PARENT(Ktwelve.PRIMARY_SCHOOL, "17Parent"),
    XUESHE(Ktwelve.JUNIOR_SCHOOL, "17Student"),
    INFANT_TEACHER(Ktwelve.INFANT, "17Teacher"),
    PRIMARY_TEACHER(Ktwelve.PRIMARY_SCHOOL, "17Teacher"),
    JUNIOR_TEACHER(Ktwelve.JUNIOR_SCHOOL, "17JuniorTea"),  // FIXME to be update to  JUNIOR_TEACHER(Ktwelve.JUNIOR_SCHOOL, "17JuniorTea")
    UNKNOWN(Ktwelve.UNKNOWN, ""),
    TRAVELAMERICA(Ktwelve.PRIMARY_SCHOOL, "17Parent"),
    SANGUODMZ(Ktwelve.PRIMARY_SCHOOL, "17Parent"),
    A17ZYSPG(Ktwelve.PRIMARY_SCHOOL, "A17ZYSPG"),
    GREATADVENTURE(Ktwelve.PRIMARY_SCHOOL, "17Parent"),
    HOMEWORK_FINISH(Ktwelve.PRIMARY_SCHOOL, "17Parent"),
    FAIRYLAND(Ktwelve.PRIMARY_SCHOOL, "17Student"),
    USAADVENTURE(Ktwelve.PRIMARY_SCHOOL, "17Parent"),
    STEM101(Ktwelve.PRIMARY_SCHOOL, "17Parent"),
    YIQIXUETEAHCER(Ktwelve.PRIMARY_SCHOOL, "YiQiXueTeacher"),
    YIQIXUEPARENT(Ktwelve.PRIMARY_SCHOOL, "17Parent"),
    YUNKETANG(Ktwelve.PRIMARY_SCHOOL, "17Yunketang"),

    JUNIOR_STUDENT(Ktwelve.JUNIOR_SCHOOL, "17JuniorStu"),
    JUNIOR_PARENT(Ktwelve.JUNIOR_SCHOOL,"17JuniorPar"),
    HONEYCOMB(Ktwelve.UNKNOWN,"honeyComb"),
    HONEYCOMB_EV(Ktwelve.UNKNOWN,"honeyCombEV"),

    AGENT(Ktwelve.PRIMARY_SCHOOL, "17Agent")
    ;

    public Ktwelve ktwelve;
    public String appKey;

    AppMessageSource(Ktwelve ktwelve, String appKey) {
        this.ktwelve = ktwelve;
        this.appKey = appKey;
    }

    private static final Map<String, AppMessageSource> map;

    static {
        map = new HashMap<>();
        for (AppMessageSource key : AppMessageSource.values()) {
            if (!map.containsKey(key.name())) {
                map.put(key.name(), key);
            }
        }
    }

    public static AppMessageSource of(String source) {
        if (source == null || source.trim().length() == 0) {
            return UNKNOWN;
        }
        return map.getOrDefault(source, UNKNOWN);
    }
}
