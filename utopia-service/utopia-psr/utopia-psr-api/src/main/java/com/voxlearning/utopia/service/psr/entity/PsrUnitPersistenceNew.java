package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/29.
 */
@Data
public class PsrUnitPersistenceNew {

    /** 单元ID */
    private String unitId;
    /** 中文名称 */
    private String name;

    private Integer rank;
    /** lessons */
    private Map<String/*lessonId*/, PsrLessonPersistenceNew> lessonPersistenceMap;
    private Map<String/*kpid*/,NewKnowledgePoint/**/> sentences;

    public PsrLessonPersistenceNew getLessonPersistenceByLessonId(String lessonId) {
        if (lessonPersistenceMap.containsKey(lessonId)) return lessonPersistenceMap.get(lessonId);

        return null;
    }

    public PsrUnitPersistenceNew() {
        lessonPersistenceMap = new HashMap<>();
        sentences = new HashMap<>();
    }
}
