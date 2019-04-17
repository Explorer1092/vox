/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author xin
 * @since 14-5-21 下午1:45
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum WechatNoticeType {
    ANONYMOUS(0, "未知类型"),
    @Deprecated
    HOMEWORK_CREATE(1, "老师布置作业"),
    @Deprecated
    HOMEWORK_CHECK(2, "老师检查作业"),
    @Deprecated
    MATH_HOMEWORK_CREATE(3, "数学老师布置作业"),
    @Deprecated
    FINISH_ENGLISH_VACATION_HOMEWORK_PACKAGE(4, "完成英语假期作业任务包"),
    @Deprecated
    FINISH_MATH_VACATION_HOMEWORK_PACKAGE(5, "完成数学假期作业任务包"),
    @Deprecated
    QUIZ_CREATE(6, "布置英语测验"),
    @Deprecated
    MATH_QUIZ_CREATE(7, "布置数学测验"),
    @Deprecated
    QUIZ_CHECK(8, "检查测验"),
    @Deprecated
    MATH_QUIZ_CHECK(9, "检查数学测验"),
    @Deprecated
    MATH_HOMEWORK_CHECK(10, "检查数学作业"),
    @Deprecated
    VACATION_HOMEWORK_CREATE(11, "布置英语假期作业"),
    @Deprecated
    MATH_VACATION_HOMEWORK_CREATE(12, "布置数学假期作业"),
    @Deprecated
    CHINESE_HOMEWORK_CREATE(13, "语文老师布置作业"),
    @Deprecated
    CHINESE_HOMEWORK_CHECK(14, "语文检查作业"),
    @Deprecated
    CHINESE_QUIZ_CREATE(15, "布置语文测验"),
    @Deprecated
    CHINESE_QUIZ_CHECK(16, "检查语文测验"),
    @Deprecated
    CHINESE_VACATION_HOMEWORK_CREATE(17, "布置语文假期作业"),
    @Deprecated
    SMARTCLAZZ_STUDENT_REWARD(18, "智慧教室学生奖励"),
    TEMPLATE_HOMEWORK_CREATE(20, "布置作业模板消息"),
    TEMPLATE_HOMEWORK_CHECK(21, "检查作业模板消息"),
    TEMPLATE_CHINESE_HOMEWORK_CHECK(22, "检查语文作业模板消息"),
    TEMPLATE_CHINESE_HOMEWORK_CREATE(23, "布置语文作业模板消息"),
    TEMPLATE_MATH_HOMEWORK_CHECK(24, "检查数学作业模板消息"),
    TEMPLATE_MATH_HOMEWORK_CREATE(25, "布置数学作业模板消息"),
    @Deprecated
    TEMPLATE_MATH_QUIZ_CHECK(26, "数学测验检查模板消息"),
    @Deprecated
    TEMPLATE_MATH_QUIZ_CREATE(27, "数学测验布置模板消息"),
    @Deprecated
    TEMPLATE_QUIZ_CHECK(28, "英语测验检查模板消息"),
    @Deprecated
    TEMPLATE_QUIZ_CREATE(29, "英语测验布置模板消息"),
    TEMPLATE_SMARTCLAZZ_STUDENT_REWARD(30, "智慧教室学生奖励"),
    @Deprecated
    TEMPLATE_INTEGRAL_TRANSFER(31, "家长送老师学豆"),
    @Deprecated
    TEMPLATE_XXT_INFORM(32, "老师发送校讯通通知"),
    @Deprecated
    TEMPLATE_HOMEWORK_REMIND(33, "未完成作业提醒消息"),
    TEMPLATE_UNPAID_ORDER_REMIND(34, "未支付订单提醒消息"),
    @Deprecated
    TEMPLATE_UNSIGNED_REMIND(35, "未签到领取奖励提醒消息"),
    @Deprecated
    TEMPLATE_AFENTI_REPORT_REMIND(36, "阿分题学习报告提醒消息"),
    @Deprecated
    TEMPLATE_DRAW_TOGETHER_NOTICE(37, "斑马小镇家长分享"),
    @Deprecated
    TEMPLATE_AFENTI_ERROR_KNOWLEDGE_NOTICE(38, "阿分题做错知识点提醒"),
    TEMPLATE_MAKE_A_WISH(39, "家长鼓励学生提交心愿"),
    @Deprecated
    TEMPLATE_REMIND_SET_MISSION(40, "家长鼓励学生提醒家长设置目标"),
    TEMPLATE_REMIND_UPDATE_PROGRESS(41, "家长鼓励学生提醒家长更新进度"),
    TEMPLATE_REMIND_REWARD(42, "家长鼓励学生提醒家长发奖"),
    TEMPLATE_TRAVEL_AMERICA_NOTICE(43, "走遍美国报告提醒"),
    @Deprecated
    TEMPLATE_YEAR_TRACK_NOTICE(44, "学生成长轨迹报告提醒"),
    @Deprecated
    TEMPLATE_XXT_MONTH_STAR_REWARD_EXPIRED_REMIND(45, "校讯通星星月排行奖励过期提醒"),
    @Deprecated
    TEMPLATE_XXT_TERM_STAR_REWARD_EXPIRED_REMIND(46, "校讯通星星学期排行奖励过期提醒"),
    TEMPLATE_PARENT_MISSION_REWARD_EXPIRED_REMIND(47, "家长奖励赠送学豆过期提醒"),
    @Deprecated
    TEMPLATE_RUSH_TO_PURCHASE_REMIND(48, "抢购商品提醒"),
    TEMPLATE_EXPIRE_ACTIVATE_AFENTI_ORDER_REMIND(49, "到期产品续费提醒"),
    @Deprecated
    TEMPLATE_PARENT_MOTHERS_DAY_CARD(50, "母亲节贺卡"),
    TEMPLATE_PARENT_FLOWER_GRATITUDE_REMIND(51, "家长收到老师鲜花感谢提醒"),
    @Deprecated
    TEMPLATE_PARENT_LITTLE_CHAMPION_NOTICE(52, "家长满分小状元活动提醒"),
    TEMPLATE_PARENT_APP_NOTICE(53, "老师提醒家长使用APP做作业"),
    @Deprecated
    TEMPLATE_PARENT_TEACHERS_DAY_NOTICE(54, "教师节活动提醒"),
    @Deprecated
    TEMPLATE_WORKBOOK_HOMEWORK_CREATE(55, "布置教辅作业模板消息"),
    @Deprecated
    TEMPLATE_WORKBOOK_HOMEWORK_CHECK(56, "检查教辅作业模板消息"),
    TEMPLATE__AFENTI_ORDER_AMOUNT_NOTICE(57, "每日消费提醒微信模板消息"),
    @Deprecated
    TEMPLATE_PARENT_ONLINE_QA_ANSWER_RECEIVED(58, "问题已解答提醒"),
    @Deprecated
    TEMPLATE_PARENT_ONLINE_QA_COMMENT(59, "对问题的回答进行评论提醒"),
    @Deprecated
    TEMPLATE_PARENT_RESERVE_TRUSTEE_NOTICE(60, "家长成功预约托管所提醒"),
    @Deprecated
    TEMPLATE_PARENT_PAY_TRUSTEE_ORDER_NOTICE(61, "家长成功支付托管所提醒"),
    @Deprecated
    TEMPLATE_PARENT_TRUSTEE_SIGNPIC_NOTICE(62, "家长托管所成功签到图片提醒"),
    @Deprecated
    TEMPLATE_XXT_TEACHER_SEND_STAR(63, "校讯通老师检查作业后奖励星星"),
    @Deprecated
    TEMPLATE_XXT_TEACHER_SEND_INTEGRAL(64, "校讯通老师检查作业后奖励学豆"),
    @Deprecated
    TEMPLATE_THANKS_TEACHER_THANK_PARENT_NOTICE(65, "感恩节老师感谢家长通知"),
    @Deprecated
    TEMPLATE_THANKS_TEACHER_REWARD_STUDENT_NOTICE(66, "感恩节老师奖励学生通知"),
    @Deprecated
    TEMPLATE_PARENT_PAY_TRUSTEE_CLS_ORDER_NOTICE(67, "家长成功支付托管订单通知"), //新版托管班 2016-1-6
    @Deprecated
    TEMPLATE_PARENT_TRUSTEE_ORDER_ACTIVATE_NOTICE(68, "托管学习券激活通知"),
    TEMPLATE_PARENT_REMIND_FOR_GRADUATE_STUDENT_FROM_REWARD(69, "家长毕业班学生奖品中心提醒消息"),
    TEMPLATE_PARENT_OPERATIONAL_NOTICE(70, "家长微信导流用微信模版消息"),

    //Teacher Notice
    TEACHER_TEMPLATE_HOMEWORK_EXPIRE_REMIND(1000, "到期未检查的作业给老师发微信消息"),
    TEACHER_TEMPLATE_ACCEPT_INVITE_REMIND(1001, "被邀请的老师注册后发微信消息给邀请者"),
    TEACHER_TEMPLATE_REMIND_STUDENT_LOGIN_REMIND(1002, "提醒老师让学生登录"),
    TEACHER_TEMPLATE_ASSIGN_HOMEWORK_EXPERIENCE_REMIND(1003, "老师布置作业体验"),
    TEACHER_TEMPLATE_CREATE_CLAZZ_REMIND(1004, "提醒老师建班"),
    @Deprecated
    TEACHER_TEMPLATE_NOT_CREATE_CLAZZ_REMIND(1005, "提醒老师只有少于2个学生登录过"),
    TEACHER_TEMPLATE_STUDENT_COUNT_MORE_THAN_2_REMIND(1006, "老师已建班学生登录数量大于2"),
    TEACHER_TEMPLATE_CERTIFICATE_REMIND(1007, "老师认证后微信消息提醒"),
    TEACHER_TEMPLATE_INTEGRAL_REMIND(1008, "老师积分变动提醒"),
    TEACHER_TEMPLATE_CRM_REMIND(1009, "CRM中给老师发微信消息"),
    TEACHER_TEMPLATE_FLOWER_RECEIVED_REMIND(1010, "老师收到鲜花提醒"),
    @Deprecated
    TEACHER_TEMPLATE_LITTLE_CHAMPION_NOTICE(1011, "老师满分小状元活动审核提醒"),
    TEACHER_TEMPLATE_MORE_STU_NOTICE(1012, "教学生加入一起作业提醒"),
    TEACHER_TEMPLATE_WIRELESS_CHARGING_NOTICE(1013, "话费充值提醒"),
    @Deprecated
    TEACHER_TEMPLATE_TEACHERS_DAY_NOTICE(1014, "教师节活动提醒"),
    @Deprecated
    TEACHER_TEMPLATE_CLAZZ_ALTERATION_NOTICE(1015, "教师换班提醒"),
    TEACHER_RED_FRIDAY_NOTICE(1016, "最红星期五通知"),
    @Deprecated
    TEACHER_THANKS_PARENT_SEND_INTEGRAL_NOTICE(1017, "感恩节家长给班级送学豆通知"),
    @Deprecated
    TEACHER_THANKS_PARENT_THANK_TEACHER_NOTICE(1018, "感恩节家长感谢老师通知"),
    @Deprecated
    TEACHER_THANKS_STUDENT_TARGET_NOTICE(1019, "感恩节学生设置目标通知"),
    TEACHER_CHRISTMAS_PARENT_SEND_INTEGRAL_NOTICE(1020, "圣诞节家长给班级送学豆通知"),
    TEACHER_FLOWER_EXCHANGE_EXPIRE_REMIND_NOTICE(1021, "鲜花兑换学豆每月即将到期提醒"),
    TEACHER_REMIND_FOR_GRADUATE_STUDENT_FROM_REWARD(1022, "老师毕业班学生奖品中心提醒消息"),
    TEACHER_OPERATION_NOTICE(1023, "老师微信运营类消息"),
    TEACHER_CHEATING_NOTICE(1024, "老师作弊作业提醒"),
    TEACHER_FAKE_NOTICE(1025, "老师判假提醒"),

    //App Teacher created Notice
    @Deprecated
    TEMPLATE_TEACHER_TO_PARENT_NOTICE(2000, "老师端App发送消息给家长"),


    // temp type 这些都是py端插入的，没经过后台。为了便于crm显示，暂时在这里加上字典
    @Deprecated
    TEMPLATE_PARENT_SUMMER_VOCATION_HOMEWORK_NOTICE(7008, "暑假作业报告提醒");

    private static Map<Integer, WechatNoticeType> types;
    @Getter
    private int type;
    @Getter
    private String description;

    WechatNoticeType(int type, String description) {
        this.type = type;
        this.description = description;
    }

    static {
        types = new LinkedHashMap<>();
        for (WechatNoticeType type : values()) {
            types.put(type.getType(), type);
        }
    }

    public static WechatNoticeType of(Integer type) {
        if (null == type) {
            return ANONYMOUS;
        }
        return types.get(type);
    }

    public static WechatNoticeType of(String value) {
        try {
            return WechatNoticeType.valueOf(value);
        } catch (Exception ignored) {
            return ANONYMOUS;
        }
    }
}
