package com.voxlearning.utopia.service.reminder.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author shiwei.liao
 * @since 2017-5-9
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum ReminderPosition {
    PARENT_APP_NEW_INDEX_CHILD(15, Boolean.FALSE),//新版家长端首页孩子列表的红点提示
    @Deprecated
    PARENT_APP_TAB_GROWTH(15, Boolean.FALSE),
    @Deprecated
    PARENT_APP_TAB_STUDY(15, Boolean.FALSE),
    @Deprecated
    PARENT_APP_TAB_LIVE(15, Boolean.FALSE),
    @Deprecated
    PARENT_APP_TAB_PERSON(15, Boolean.FALSE),
    PARENT_APP_TAB_TALK(1, Boolean.FALSE),
    PARENT_APP_TAB_STUDY_RESOURCE(15, Boolean.FALSE),
    PARENT_APP_TAB_EXCELLENT_CLASS(15, Boolean.FALSE),
    PARENT_APP_TAB_STUDY_PROGRESS(15, Boolean.FALSE),
    PARENT_APP_TAB_PARENT_TALK(15, Boolean.FALSE),
    PARENT_APP_TAB_PERSON_CENTER(15, Boolean.FALSE),
    @Deprecated
    PARENT_APP_EASEMOB_INDEX_EXT_NOTICE(30, Boolean.FALSE),
    @Deprecated
    PARENT_APP_EASEMOB_INDEX_EXT_EXPAND(30, Boolean.FALSE),
    //下面这俩是当天过期 cache manager里面做了处理
    PARENT_APP_EASEMOB_BOTTOM_MENU_NOTIFY(1, Boolean.FALSE),
    PARENT_APP_EASEMOB_BOTTOM_MENU_EXPAND(30, Boolean.TRUE),
    GW_XXLB(1, Boolean.FALSE), // 成长世界学习礼包红点
    GW_TASK(1, Boolean.FALSE), // 成长世界任务红点
    GW_ISLAND_ENGLISH(1, Boolean.FALSE), // 成长世界英语岛红点
    GW_ISLAND_MATH(1, Boolean.FALSE), // 成长世界数学岛红点
    GW_ISLAND_CHINESE(1, Boolean.FALSE), // 成长世界语文岛红点
    GW_ISLAND_WISDOM(1, Boolean.FALSE), // 成长世界智慧岛红点
    GW_MEDAL(1, Boolean.FALSE), // 成长世界勋章红点
    PARENT_APP_INDEX_MY_STUDY_ALBUM(15, Boolean.TRUE),
    PARENT_APP_EASEMOB_INDEX_EXT(30, Boolean.FALSE),
    TAU_HANDBOOK(1, Boolean.FALSE), // 环游欧洲攻略红点
    GW_LOTTO_TASK(1, Boolean.FALSE), // 成长世界抽奖模块抽奖卡任务红点
    WJ_TASK(1, Boolean.FALSE), // 西游记活动任务小红点
    ;

    private int expireDay;
    private Boolean showNumber;

    ReminderPosition(int day, Boolean show) {
        this.expireDay = day;
        this.showNumber = show;
    }

    public static ReminderPosition of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
