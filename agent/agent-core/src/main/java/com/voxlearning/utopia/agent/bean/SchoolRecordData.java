package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2015/11/19
 */
@Getter
@Setter
public class SchoolRecordData implements Serializable {
    private static final long serialVersionUID = 6199294821513355982L;

    private Long schoolId;                  // 学校ID
    private String schoolName;              // 学校名称
    private int teacherTotal;              // 老师总数
    private int teacherRegCountBefore;    // 进校前老师注册数
    private int teacherRegRateBefore;     // 进校前老师注册率
    private int teacherAuthCountBefore;   // 进校前老师认证数
    private int teacherAuthRateBefore;    // 进校前老师认证率
    private int teacherRegCount7Days;     // 7天后老师注册数
    private int teacherRegRate7Days;      // 7天后老师注册率
    private int teacherAuthCount7Days;    // 7天后老师认证数
    private int teacherAuthRate7Days;     // 7天后老师认证率
    private int teacherRegCount14Days;    // 14天后老师注册数
    private int teacherRegRate14Days;     // 14天后老师注册率
    private int teacherAuthCount14Days;   // 14天后老师认证数
    private int teacherAuthRate14Days;    // 14天后老师认证率
    private int teacherRegCount;           // 最新老师注册数
    private int teacherRegRate;            // 最新老师注册率
    private int teacherAuthCount;          // 最新老师认证数
    private int teacherAuthRate;           // 最新老师认证率
    private int studentTotal;              // 学生总数
    private int studentRegCountBefore;    // 进校前学生注册数
    private int studentRegRateBefore;     // 进校前学生注册率
    private int studentAuthCountBefore;   // 进校前学生认证数
    private int studentAuthRateBefore;    // 进校前学生认证率
    private int studentRegCount7Days;     // 7天后学生注册数
    private int studentRegRate7Days;      // 7天后学生注册率
    private int studentAuthCount7Days;    // 7天后学生认证数
    private int studentAuthRate7Days;     // 7天后学生认证率
    private int studentRegCount14Days;    // 14天后学生注册数
    private int studentRegRate14Days;     // 14天后学生注册率
    private int studentAuthCount14Days;   // 14天后学生认证数
    private int studentAuthRate14Days;    // 14天后学生认证率
    private int studentRegCount;           // 最新学生注册数
    private int studentRegRate;            // 最新学生注册率
    private int studentAuthCount;          // 最新学生认证数
    private int studentAuthRate;           // 最新学生认证率

    public SchoolRecordData(Long schoolId, String schoolName) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
    }
}
