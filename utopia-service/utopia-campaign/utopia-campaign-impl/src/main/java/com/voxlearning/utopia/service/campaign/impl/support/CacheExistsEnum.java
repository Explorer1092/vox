package com.voxlearning.utopia.service.campaign.impl.support;

import lombok.Getter;

public enum CacheExistsEnum {

    // 是否打扰过家长
    PARENT_NOTICE_ED("NEW_TERM:PARENT_NOTICE_ED:"),
    // 学生自主参与是否打扰过家长
    STUDENT_JOIN_NOTICE("NEW_TERM:STUDENT_JOIN_notice"),
    // 学生自主参与
    STUDENT_PARTICIPATE("NEW_TERM:STUDENT_PARTICIPATE"),

    // 是否打扰过家长
    WARM_HEART_PARENT_NOTICE_ED("WARM_HEART:PARENT_NOTICE_ED:"),
    // 是否首次打卡
    WARM_HEART_PARENT_FIRST_CARD("WARM_HEART:PARENT_FIRST_CARD:"),
    // 学生首次制定计划
    WARM_HEART_PARENT_FIRST_SET_TARGET("WARM_HEART:PARENT_FIRST_SET_TARGET:"),
    // 首个计划打卡满21天
    @Deprecated
    WARM_HEART_PARENT_FIRST_CARD_21("WARM_HEART:PARENT_FIRST_CARD_21:"),
    // 首次计划结束
    WARM_HEART_PARENT_FIRST_END_30("WARM_HEART:PARENT_FIRST_END_30:"),
    ;


    @Getter
    private String key;

    CacheExistsEnum(String key) {
        this.key = key;
    }

}
