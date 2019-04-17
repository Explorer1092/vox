package com.voxlearning.utopia.service.newexam.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum NewExamPublishMessageType {

    assignApply("老师布置报名"),
    joinApply("学生参与报名考试"),
    submitApply("学生报名考试交卷"),
    shareReport("老师分享报告"),
    assignIndependent("老师布置自主考试"),
    adjustIndependent("老师调整自主考试时间"),
    submitIndependent("学生自主考试交卷"),
    studentIndependentResult("学生自主考试答题信息"),
    UNKNOWN("未知");

    @Getter
    private final String desc;

    public static NewExamPublishMessageType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
