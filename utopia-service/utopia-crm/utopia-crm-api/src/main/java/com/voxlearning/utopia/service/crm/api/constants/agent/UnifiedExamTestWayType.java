package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yagaung.wang
 * on 2017/10/25.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum UnifiedExamTestWayType {
    RANDOM(1, "完全随机获取"),
    ROLL_POLING(0, "轮训获取");

    @Getter
    private final int type;
    @Getter
    private final String desc;
    private static final Map<Integer, UnifiedExamTestWayType> testWayMap;
    static {
        testWayMap = new HashMap<>();
        for (UnifiedExamTestWayType type : UnifiedExamTestWayType.values()) {
            testWayMap.put(type.getType(), type);
        }
    }

    public static UnifiedExamTestWayType of(Integer type){
        if(type == null){
            return null;
        }
        return testWayMap.get(type);
    }

    public static UnifiedExamTestWayType of(String type) {
        try {
            return UnifiedExamTestWayType.valueOf(type);
        } catch (Exception ex) {
            return null;
        }
    }
}
