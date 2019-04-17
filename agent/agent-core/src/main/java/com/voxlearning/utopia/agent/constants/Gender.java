package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 性别枚举
 * Created by yaguang.wang on 2016/10/19.
 */
@Getter
@RequiredArgsConstructor
public enum Gender {
    MAN(1, "男"),
    WOMAN(2, "女");

    private final int type;
    private final String typeName;

    private static final Map<Integer, Gender> genderMap;

    static {
        genderMap = new HashMap<>();
        for (Gender gender : Gender.values()) {
            genderMap.put(gender.getType(), gender);
        }
    }

    public static Gender typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return Gender.genderMap.get(id);
    }
}
