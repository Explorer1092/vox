package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 统考试卷类型
 * Created by tao.zang on 2017/4/17.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum  UnifiedExamTestPaperType {
    ORALLANGUAGE(1,"口语考试"),
    UNORALLANGUAGE(3,"非口语考试"),
    COMPREHENSIVEEXAMINATION(2, "口语+非口语综合考试"),
    NORMAL(5, "普通考试"),
    ORAL(6, "口语考试"),
    LISTENING(7, "听力考试");
    @Getter
    private final int type;
    @Getter
    private final String desc;

    private static final Map<Integer, UnifiedExamTestPaperType> testPaperMap;
    static {
        testPaperMap = new HashMap<>();
        for (UnifiedExamTestPaperType type : UnifiedExamTestPaperType.values()) {
            testPaperMap.put(type.getType(), type);
        }
    }
    public static UnifiedExamTestPaperType of(Integer type){
        if(type == null){
            return null;
        }
        return testPaperMap.get(type);
    }

    public static UnifiedExamTestPaperType of(String type) {
        try {
            return UnifiedExamTestPaperType.valueOf(type);
        } catch (Exception ex) {
            return null;
        }
    }
}
