package com.voxlearning.washington.controller.thirdparty.qiyukf.model;

import lombok.Getter;

import java.util.Arrays;

/**
 * 七鱼在线客服配置
 *
 * @author Wenlong Meng
 * @version 1.0
 * @date 2018-09-03
 */
public enum QiYuKFConfig {
    //【小学学生端】
    student_question_account(439020, 27001, 90958, "小学-student-账号密码"),
    student_question_homework(437260, 26001, 90958, "小学-student-作业问题"),
    student_question_class(435479, 28000, 90958, "小学-student-班级相关"),
    student_question_other(439023, 28025, 90958, "小学-student-其他"),
    student_question_advice(436638, 54044, 90958, "小学-student-给我们提建议"),
    student_question_zengzhi(439021, 34386, 90958, "小学-student-增值"),
    student_question_award(435481, 29000, 90958, "小学-student-奖品相关"),

    //【中学学生端】
    juniorstu_question_other(1302933, 39198, 195647, "中学-student-其他"),

    //【小学老师端】
    teacher_question_other(436649, 34389, 175697, "小学-teacher-联系客服"),
    teacher_question_advice(436649, 0, 0, "小学-teacher-投诉建议"),

    //【中学老师端】
    juniortea_question_other_math(1302934, 36178, 175697, "中学-teacher-数学"),
    juniortea_question_other_english(1302934, 1213002, 175697, "中学-teacher-英语"),
    juniortea_question_advice_english(1302934, 0, 0, "中学-teacher-英语-建议"),

    //【家长端】
    parent_question_jingpinke(980233, 49030, 195646, "家长通-精品课程"),
    parent_question_other(435533, 46083, 187864, "家长通-帮助中心主页"),
    parent_question_advice(437318, 1204003, 187864, "家长通-帮助中心主页"),
    ;

    @Getter
    int csGroupId;//客服分组id
    @Getter
    int qtype;//问题模板
    @Getter
    int robotId;
    String desc;//问题类型

    /**
     * 构建七鱼客服配置信息
     *
     * @param csGroupId
     * @param qtype
     * @param robotId
     * @param desc
     */
    QiYuKFConfig(int csGroupId, int qtype, int robotId, String desc){
        this.csGroupId = csGroupId;
        this.qtype = qtype;
        this.robotId = robotId;
        this.desc = desc;
    }
    /**
     * 查询名称对应的七鱼在线客服配置
     *
     * @param name
     * @return
     */
    public static QiYuKFConfig nameOf(String name){
        return Arrays.stream(QiYuKFConfig.values()).filter(t -> t.name().equals(name)).findFirst().orElse(null);
    }

}
