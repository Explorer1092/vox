package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 统考考试场景
 * Created by yaguang.wang
 * on 2017/10/26.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum UnifiedExamTestPaperScene {

    ONLINE_EXAM(0, "在线考试"),
    CONCENTRATE_EXAM(1, "集中考试");

    @Getter
    private final int type;
    @Getter
    private final String desc;

    private static final Map<Integer, UnifiedExamTestPaperScene> testSceneMap;
    static {
        testSceneMap = new HashMap<>();
        for (UnifiedExamTestPaperScene type : UnifiedExamTestPaperScene.values()) {
            testSceneMap.put(type.getType(), type);
        }
    }

    public static UnifiedExamTestPaperScene of(Integer type){
        if(type == null){
            return null;
        }
        return testSceneMap.get(type);
    }

    public static UnifiedExamTestPaperScene of(String type) {
        try {
            return UnifiedExamTestPaperScene.valueOf(type);
        } catch (Exception ex) {
            return null;
        }
    }
}
