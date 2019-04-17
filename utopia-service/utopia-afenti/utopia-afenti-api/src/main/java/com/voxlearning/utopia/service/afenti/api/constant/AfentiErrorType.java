package com.voxlearning.utopia.service.afenti.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Ruib
 * @since 2016/7/27
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AfentiErrorType {
    DEFAULT("100", "数据异常，重试一下吧"),
    NEED_LOGIN("101", "请重新登录"),
    NO_CLAZZ("102", "班级异常，加入正确的班级后再来吧"),
    DUPLICATED_OPERATION("103", "正在处理，请不要重复提交"),
    PUSH_QUESTION_OVER_TRIAL_LIMITATION("104", "你已经完成试用，如果想继续使用请购买阿分题"),
    PUSH_QUESTION_OVER_THREE_LIMITATION("105", "亲爱的同学，今天开启新关卡的机会已经用完了哦，明天再来继续学习吧"),
    PUSH_QUESTION_BALANCE_NOT_ENOUGH("106", "学豆不足，去获取更多的学豆再来开启新关卡吧"),
    INVITE_COUNT_LIMIT("107", "邀请次数限制"),
    PUSH_QUESTION_BALANCE_NOT_ENOUGH_FOR_PREPARATION("108", "如果想使用预习功能，请购买阿分题"),
    PUSH_VIDEO_NOT_ENOUGH_FOR_PREPARATION("109", "如果想观看预习视频，请购买阿分题"),
    DATA_ERROR("110", "数据维护中，请过5分钟再重试一下"),
    NO_QUESTION("111", "抱歉，本关卡正在升级维护中，请先闯其他关卡吧！"),
    BOOK_NOT_MACH_GRADE("112", "您的年级与所选的教材不匹配，请先更换教材！"),
    NO_PAID_OR_OUT_OF_DATE("113", "您还没有开通阿分题或者已经过期"),
    PUSH_REVIEW_QUESTION_OVER_THREE_LIMITATION("114", "每天最多开启3个复习关卡哦，明天再来继续复习吧！");

    @Getter private final String code;
    @Getter private final String info;
}
