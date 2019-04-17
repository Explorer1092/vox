package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.util.Map;

@Data
public class PsrUnitPersistence {

    /** 单元ID */
    private Long unitId;
    /** 中文名称 */
    private String cname;
    /** 英文名称 */
    private String ename;
    private Integer rank;
    /** lessons */
    private Map<Long/*lessonId*/, PsrLessonPersistence> lessonPersistenceMap;

    public PsrLessonPersistence getLessonPersistenceByLessonId(Long lessonId) {
        if (lessonPersistenceMap.containsKey(lessonId)) return lessonPersistenceMap.get(lessonId);

        return null;
    }
}
