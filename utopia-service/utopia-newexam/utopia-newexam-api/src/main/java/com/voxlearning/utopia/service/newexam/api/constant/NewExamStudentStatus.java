package com.voxlearning.utopia.service.newexam.api.constant;

import lombok.Getter;

/**
 * 学生考试状态
 *
 * @author guoqiang.li
 * @since 2016/3/14
 */
public enum NewExamStudentStatus {
    EXPIRED("已过期"),
    REGISTRABLE("可报名"),
    REGISTERED("已报名"),
    BEGIN("开始考试"),
    CONTINUE("继续考试"),
    ABSENT("缺考"),
    ISSUING("待发布成绩"),
    NOT_ALLOW_VIEW_SCORE("不允许学生查看成绩"),
    END("已完结");

    @Getter
    private final String desc;

    NewExamStudentStatus(String desc) {
        this.desc = desc;
    }

    public static NewExamStudentStatus of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
