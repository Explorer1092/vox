
package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum WechatTemplateMessageType {
    ANONYMOUS(0, "未知类型", WechatType.AMBASSADOR),
    CHIPS_BUY_SUCCESS(1, "薯条课程报名成功", WechatType.CHIPS),
    CHIPS_COURSE_BEGIN(2, "薯条课程开课提醒", WechatType.CHIPS),
    CHIPS_STUDY_DAILY_NOTIFY(3, "薯条学习计划提醒", WechatType.CHIPS),
    CHIPS_STUDY_DAILY_SUMMARY(5, "薯条学习总结提醒", WechatType.CHIPS),
    CHIPS_INVITATION_SUCCESS(6, "薯条课程推荐成功通知", WechatType.CHIPS),
    CHIPS_COURSE_END(7, "薯条课程到期提醒", WechatType.CHIPS),
    CHIPS_COURSE_FEEDBACK(8, "薯条课程反馈通知", WechatType.CHIPS),
    CHIPS_COURSE_DAILY_RANK(9, "薯条课程每日排行榜", WechatType.CHIPS),
    CHIPS_COURSE_GRADUATION_CERTIFICATE(10, "薯条课程毕业证书", WechatType.CHIPS),
    CHIPS_COURSE_DAILY_LESSON(11, "薯条课程今日学习", WechatType.CHIPS),
    CHIPS_DAILY_SHARE_RECORD_REMIND(12, "薯条每日打卡提醒", WechatType.CHIPS),
    CHIPS_GROUP_SHOPPING_SUCCESS(13, "薯条拼团成功", WechatType.CHIPS),
    CHIPS_AD_VISIT_NOTIFY(14, "薯条广告页浏览提醒", WechatType.CHIPS),
    CHIPS_INVITATION_ORDER_NOTIFY(15, "薯条课程推荐订单分红通知", WechatType.CHIPS),
    CHIPS_INVITATION_BEGIN_NOTIFY(16, "薯条课程推荐活动即将开始通知", WechatType.CHIPS),
    CHIPS_INVITATION_TEAM_COM_NOTIFY(17, "薯条课程推荐活动组队提醒", WechatType.CHIPS),
    CHIPS_INVITATION_COM_FAIL_NOTIFY(18, "薯条课程推荐活动助力失败通知", WechatType.CHIPS),
    CHIPS_CLAZZ_CHANG_TEACHER_NOTIFY(19, "薯条班级换老师通知", WechatType.CHIPS);

    private static Map<Integer, WechatTemplateMessageType> types;
    @Getter
    private int type;
    @Getter
    private String description;

    @Getter
    private WechatType wechatType;

    WechatTemplateMessageType(int type, String description, WechatType wechatType) {
        this.type = type;
        this.description = description;
        this.wechatType = wechatType;
    }

    static {
        types = new LinkedHashMap<>();
        for (WechatTemplateMessageType type : values()) {
            types.put(type.getType(), type);
        }
    }

    public static WechatTemplateMessageType of(Integer type) {
        if (null == type) {
            return ANONYMOUS;
        }
        return types.get(type);
    }

    public static WechatTemplateMessageType of(String value) {
        try {
            return WechatTemplateMessageType.valueOf(value);
        } catch (Exception ignored) {
            return ANONYMOUS;
        }
    }
}
