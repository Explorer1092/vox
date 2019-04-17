package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/9/20.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarCourseCategory {
    GOOD_COURSE("好课试听"),
    DAY_COURSE("每日一课"),
    VIDEO_COURSE("精品视频课程"),
    PARENTAL_ACTIVITY("亲子活动"),
    OPEN_LIVE_COURSE("公开课"),
    NORMAL_LIVE_COURSE("长期课"),
    MICRO_COURSE_OPENING("微课堂-公开课"),
    MICRO_COURSE_NORMAL("微课堂-长期课"),
    ;

    @Getter private final String desc;

    private static final Map<String, MizarCourseCategory> buffer;

    static {
        buffer = new HashMap<>();
        for (MizarCourseCategory inst : values()) {
            buffer.put(inst.name(), inst);
        }
    }

    public static MizarCourseCategory of(String category) {
        return buffer.get(category);
    }
}
