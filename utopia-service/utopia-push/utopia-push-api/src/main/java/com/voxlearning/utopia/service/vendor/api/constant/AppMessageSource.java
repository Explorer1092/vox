package com.voxlearning.utopia.service.vendor.api.constant;

import com.voxlearning.alps.annotation.meta.Ktwelve;

import java.util.HashMap;
import java.util.Map;

/**
 * 为保证接口兼容，暂时保留此类
 * 新功能使用 {@link com.voxlearning.utopia.service.push.api.constant.AppMessageSource}
 */
@Deprecated
public enum AppMessageSource {
    STUDENT(Ktwelve.PRIMARY_SCHOOL, "17Student"),
    PARENT(Ktwelve.PRIMARY_SCHOOL, "17Parent"),
    XUESHE(Ktwelve.JUNIOR_SCHOOL, "17Student"),
    PRIMARY_TEACHER(Ktwelve.PRIMARY_SCHOOL, "17Teacher"),
    JUNIOR_TEACHER(Ktwelve.JUNIOR_SCHOOL, "17JuniorTea"),
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