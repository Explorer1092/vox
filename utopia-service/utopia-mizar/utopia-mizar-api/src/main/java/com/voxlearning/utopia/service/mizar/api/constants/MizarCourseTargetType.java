package com.voxlearning.utopia.service.mizar.api.constants;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Summer Yang on 2016/9/21.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarCourseTargetType {
    TARGET_TYPE_UNKNOWN(-1, "未知类型"),
    TARGET_TYPE_REGION(1, "广告投放地区"),
    TARGET_TYPE_SCHOOL(2, "广告投放学校"),
    TARGET_TYPE_ALL(3, "投放所有用户"),;

    @Getter private final int type;
    @Getter private final String desc;

    public static MizarCourseTargetType of(Integer type) {
        for (MizarCourseTargetType target : values()) {
            if (target.getType() == type) {
                return target;
            }
        }
        return TARGET_TYPE_UNKNOWN;
    }

    public static List<KeyValuePair<Integer, String>> toKeyValuePairs() {
        List<KeyValuePair<Integer, String>> pairs = new ArrayList<>();
        for (MizarCourseTargetType targetType : values()) {
            if (targetType.getType() <= 0) continue;
            KeyValuePair<Integer, String> pair = new KeyValuePair<>();
            pair.setKey(targetType.type);
            pair.setValue(targetType.desc);
            pairs.add(pair);
        }
        return pairs;
    }

}
