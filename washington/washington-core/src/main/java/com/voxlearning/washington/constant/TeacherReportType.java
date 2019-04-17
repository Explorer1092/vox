package com.voxlearning.washington.constant;

import lombok.Getter;

/**
 * @Description: 教师端报告类型
 * @author: Mr_VanGogh
 * @date: 2019/4/1 下午6:14
 */
public enum TeacherReportType {

    DAILY_REPORT("作业日报"),
    WEEK_REPORT("作业周报"),
    UNIT_TEST_REPORT("单元检测"),
    MOCK_REPORT("考试报告");
    //OTHER_REPORT("其他");

    @Getter
    private String name;

    TeacherReportType(String name) {
        this.name = name;
    }

    public static TeacherReportType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
