package com.voxlearning.utopia.service.campaign.api.enums;

public enum TeacherActivityEnum {

    PARENT_CHILD_2018,
    NEW_TERM_PLAN_2019,
    NEW_TERM_PLAN_2019_SING_UP,   // 新一轮新学期计划的报名(家长)
    NEW_TERM_PLAN_2019_SING_UP_T, // 新一轮新学期计划的报名(老师)
    INVI_CLICK_2019,
    WARM_HEART, //暖心亲子计划(家长)
    WARM_HEART_T, //暖心亲子计划(老师)

    ;

    public static TeacherActivityEnum safeValueOf(String name) {
        for (TeacherActivityEnum value : TeacherActivityEnum.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
