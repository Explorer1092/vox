package com.voxlearning.washington.controller.thirdparty.qiyukf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 七鱼在线客服配置
 *
 * @author Wenlong Meng
 * @version 1.0
 * @date 2018-09-03
 */
@AllArgsConstructor
public enum QiYuOnlineCSConfig {
    //【小学学生端】
    question_afenti_ps(439021, 25002, 90958, "小学-student-afenti"),
    question_kupao_ps(439021, 25002, 90958, "小学-student-酷跑"),
    question_zoumei_ps(439021, 25002, 90958, "小学-student-走美"),
    question_woke_ps(439021, 25002, 90958, "小学-student-走美"),
    question_tongbulian_ps(439021, 25002, 90958, "小学-student-语文同步练"),
    question_datiaozhan_ps(439021, 25002, 90958, "小学-student-百科大挑战"),

    question_account_ps(439020, 27001, 90958, "小学-student-账号密码"),
    question_homework_ps(437260, 26001, 90958, "小学-student-作业问题"),
    question_class_ps(435479, 28000, 90958, "小学-student-班级相关"),
    question_other_ps(439023, 28025, 90958, "小学-student-其他"),
    question_advice_ps(436638, 54044, 90958, "小学-student-给我们提建议"),
    question_zengzhi_ps(439021, 34386, 90958, "小学-student-增值"),
    question_award_ps(435481, 29000, 90958, "小学-student-奖品相关"),

    //【中学学生端】
    question_account_ms(1302933, 46086, 195647, "中学-student-账号密码"),
    question_homework_ms(1302933, 34381, 195647, "中学-student-作业问题"),
    question_class_ms(1302933, 34382, 195647, "中学-student-班级相关"),
    question_award_ms(1302933, 34383, 195647, "中学-student-奖品相关"),
    question_advice_ms(1302933,0, 195647, "中学-student-给我们提建议"),

    question_other_ms(1302933, 39198, 195647, "中学-student-其他"),

    //【小学老师端】
    question_other_pt(436649, 34389, 175697, "小学-teacher-其他问题"),
    question_advice_pt(436649, 0, 0, "小学-teacher-联系客服"),

    //【中学老师端】
    question_account_mt(1302933, 36178, 175697, "中学-teacher-账号密码"),
    question_homework_mt(1302933, 36178, 175697, "中学-teacher-作业"),
    question_class_mt(1302933, 36178, 175697, "中学-teacher-班级管理"),
    question_award_mt(1302933, 36178, 175697, "中学-teacher-奖品相关"),
    question_authentication_mt(1302933, 175697, 175697, "中学-teacher-认证相关"),
    question_other_mt(1302934, 36178, 175697, "中学-teacher-其他问题"),
    question_advice_mt(1302934, 0, 0, "中学-teacher-给我们提建议"),

    //【家长端】
    question_homework_parent(435533, 46082, 187864, "家长通-作业"),
    question_zengzhi_parent(434825, 34376, 187864, "家长通-学习产品"),
    question_award_parent(435533, 49031, 187864, "家长通-家长奖励"),
    question_yiqixuexunlianying_parent(434825, 39194, 187864, "家长通-一起学训练营"),
    question_account_parent(436647, 39195, 187864, "家长通-账号登录"),
    question_brand_parent(435533, 49032, 187864, "家长通-品牌介绍"),
    question_bangzhu_parent(435533, 46083, 187864, "家长通-帮助中心主页"),
    question_jingpinke_parent(980233, 49030, 195646, "家长通-精品课程"),

    //【直播】
    question_yiqixue(980233, 49035, 195646, "一起学直播"),
    question_yiqixuechinese(980233, 34378, 195646, "一起学语文入口"),
    question_yiqixuemath(980233, 34379, 195646, "一起学数据入口"),
    question_yiqixueenglish(980233, 46085, 195646, "一起学英语入口"),

    //【极算】
    question_jisuans(1302933, 34380, 195647, "极算学生"),
    question_jisuanp(1302933, 49036, 195647, "极算家长"),
    question_jisuant(1302933, 54037, 175697, "极算老师"),

    //【快乐学】
    question_klx(1302933, 46084, 195647, "快乐学-帮助中心"),

    //【天机】
    marketer_p(439057, 34377, 201023, "小学-天机-帮助中心"),
    marketer_m(1353601, 46100, 201023, "中学-天机-帮助中心");

    @Getter
    int csGroupId;//客服分组id
    @Getter
    int qtype;//问题模板
    @Getter
    int robotId;
    String desc;//问题类型

    /**
     * 查询名称对应的七鱼在线客服配置
     *
     * @param name
     * @return
     */
    public static QiYuOnlineCSConfig nameOf(String name){
        return Arrays.stream(QiYuOnlineCSConfig.values()).filter(t -> t.name().equals(name)).findFirst().orElse(null);
    }

}
