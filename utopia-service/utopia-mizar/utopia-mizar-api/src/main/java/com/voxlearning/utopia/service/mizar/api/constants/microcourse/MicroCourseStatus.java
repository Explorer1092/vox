package com.voxlearning.utopia.service.mizar.api.constants.microcourse;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Yuechen.Wang on 2016/9/7.
 * 审核状态
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MicroCourseStatus {
    LIVE(1, "正在直播"),       // 正在直播
    ONLINE(3, "在线"),         // 在线
    OFFLINE(6, "已下架"),      // 离线
    EXPIRE(9, "已过期"),       // 过期
    ;

    @Getter private final int order;
    @Getter private final String desc;

    private final static Map<Integer, MicroCourseStatus> STATUS_MAP = new LinkedHashMap<>();

    static {
        for (MicroCourseStatus status : MicroCourseStatus.values()) {
            STATUS_MAP.put(status.getOrder(), status);
        }
    }

    public static MicroCourseStatus parse(int order) {
        return STATUS_MAP.get(order);
    }


    public static MicroCourseStatus parse(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        try {
            return valueOf(status);
        } catch (Exception ignored) {
            return null;
        }
    }
}
