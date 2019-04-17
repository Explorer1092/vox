package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author shiwe.liao
 * @since 2016/4/12
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Deprecated
public enum IMMessageType {
    MESSAGE_COMMON("MESSAGE_COMMON","type_common_template",""),//通用模板,需要自己指定tag
    MESSAGE_CHECK_HW("MESSAGE_HOMEWORK","type_check_hw","作业已检查"),
    MESSAGE_ASSIGN_HW("MESSAGE_HOMEWORK","type_check_hw","新作业"),
    MESSAGE_NOTIFY("MESSAGE_NOTIFY","type_notify","通知"),
    MESSAGE_SHARE("MESSAGE_SHARE","type_share","作业共享"),
    MESSAGE_PRAISE("MESSAGE_PRAISE","type_hint_message",""),//点赞，无tag
    MESSAGE_REMIND_EXPIRE("MESSAGE_REMIND","type_hint_message",""),//通知到期提醒，无tag
    MESSAGE_RECOMMEND_RADIO("MESSAGE_RECOMMEND_RADIO","type_recommend_radio","表扬"),
    MESSAGE_OFFLINE_HOMEWORK("MESSAGE_OFFLINE_HOMEWORK","type_notify","新作业"),
    MESSAGE_VACATION_HOMEWORK("MESSAGE_COMMON","type_check_hw","假期作业"),
    MESSAGE_JZT_VACATION_HOMEWORK("MESSAGE_COMMON","type_check_hw","寒假作业"),
    MESSAGE_AUTO_TERM_REPORT("MESSAGE_COMMON","type_common_template","学期报告"),
    MESSAGE_AUTO_TERM_REVIEW("MESSAGE_COMMON","type_common_template","期末复习"),
    MESSAGE_TEACHER_RECOMMEND_NEWS("MESSAGE_COMMON_IMAGE","type_common_template_image","学习资源"),
    MESSAGE_NOTICE_IMG("MESSAGE_COMMON_IMAGE", "type_common_template_image", ""),//图文公告
    MESSAGE_NOTICE("MESSAGE_COMMON", "type_common_template", ""),//文本公告,
    MESSAGE_ASSIGN_EXAM("MESSAGE_COMMON", "type_check_hw", "新测试"),
    MESSAGE_EXAM_REPORT("MESSAGE_COMMON", "type_check_hw", "成绩单"),
    MESSAGE_WEEK_REPORT("MESSAGE_HOMEWORK","type_check_hw","作业周报告"),
    MESSAGE_URGE_HOMEWORK("MESSAGE_HOMEWORK","type_check_hw","督促"),
    MESSAGE_URGE_CORRECT_HOMEWORK("MESSAGE_HOMEWORK","type_check_hw","督促"),
    MESSAGE_HOMEWORK_SUMMARY("MESSAGE_HOMEWORK","type_check_hw","作业小结"),
    MESSAGE_EVALUATION("MESSAGE_HOMEWORK","type_check_hw","单元测评")
    ;

    private final String message_type;
    private final String native_ui_type;
    private final String tag;

    public static IMMessageType nameOf(String name) {
        try {
            return valueOf(name);
        }catch (Exception e){
            return null;
        }
    }
}
