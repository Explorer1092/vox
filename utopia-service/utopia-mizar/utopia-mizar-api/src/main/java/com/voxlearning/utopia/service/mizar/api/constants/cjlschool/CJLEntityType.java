package com.voxlearning.utopia.service.mizar.api.constants.cjlschool;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 对应的陈经纶学校实体类型
 * Created by Yuechen.Wang on 2017/7/19.
 */
public enum CJLEntityType {

    CLASS("CIFEclass"),        // 班级
    TEACHER("CIFTeacher"),     // 老师
    STUDENT("CIFStudents"),    // 学生
    TEACHER_COURSE("CIFTeacherCourse"),  // 老师任课
    ;

    @Getter private final String entityName;

    CJLEntityType(String entityName) {
        this.entityName = entityName;
    }

    private static final Map<String, CJLEntityType> entityNameMap;

    static {
        entityNameMap = Stream.of(values()).collect(Collectors.toMap(CJLEntityType::getEntityName, Function.identity()));
    }

    public static CJLEntityType parse(String entityName) {
        if (StringUtils.isBlank(entityName)) {
            return null;
        }
        return entityNameMap.getOrDefault(entityName, null);
    }

}