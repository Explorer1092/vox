package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PsrMathUnitPersistence {

    private Long unitId;
    private String cname;
    private Integer rank;

    /** lessons */
    private Map<Long, PsrMathLessonPersistence> mathLessonPersistenceMap;
    /** 记录lesson的学习顺序 */
    private List<Long> mathLessonList;

    public PsrMathUnitPersistence() {
        mathLessonPersistenceMap = new HashMap<>();
        mathLessonList = new ArrayList<>();
    }

    public PsrMathLessonPersistence getLessonPersistenceByLessonId(Long lessonId) {
        if (mathLessonPersistenceMap != null && mathLessonPersistenceMap.containsKey(unitId))
            return mathLessonPersistenceMap.get(unitId);

        return null;
    }

    // 调试使用
    public String formatToString() {
        String retStr = "{unitId:" + unitId.toString() + " rank:" + rank.toString() + " cname:" + cname;
        for (Long lesson : mathLessonList) {
            if (mathLessonPersistenceMap.containsKey(lesson))
                retStr += mathLessonPersistenceMap.get(lesson).formatToString();
        }

        retStr += "}";
        return retStr;
    }
}
