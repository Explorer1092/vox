package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 分级制
 * Created by yaguang.wang
 * on 2017/10/26.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum UnifiedExamTestGradeType {

    SCORE(0, "分数制"),
    GRADE(1, "等第制");

    @Getter
    private final int type;
    @Getter
    private final String desc;

    private static final Map<Integer, UnifiedExamTestGradeType> gradeMap;

    static {
        gradeMap = new HashMap<>();
        for (UnifiedExamTestGradeType type : UnifiedExamTestGradeType.values()) {
            gradeMap.put(type.getType(), type);
        }
    }

    public static UnifiedExamTestGradeType of(Integer type) {
        if (type == null) {
            return null;
        }
        return gradeMap.get(type);
    }
}
