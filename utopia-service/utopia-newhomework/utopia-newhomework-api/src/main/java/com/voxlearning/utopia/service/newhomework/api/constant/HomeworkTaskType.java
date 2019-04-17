package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author guoqiang.li
 * @since 2017/4/14
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum HomeworkTaskType {
    DAILY_HOMEWORK("日常作业", 2, "resources/app/17teacher/res/daily_banner.png", "/view/tasks/dailyhomework"),
    WEEKEND_HOMEWORK("周末作业", 3, "resources/app/17teacher/res/weekend_banner.png", "/view/tasks/weekendhomework"),
    VACATION_HOMEWORK("假期作业", 0, "", ""),
    ACTIVITY_HOMEWORK("运营活动作业", 1, "", "/view/tasks/activityhomework");             // 小长假（五一、端午），雾霾 这些都属于运营活动作业

    @Getter private final String description;
    @Getter private final int priority;             // 任务优先级，越小越高
    @Getter private final String imgUrl;
    @Getter private final String detailUrl;

    public static HomeworkTaskType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ignored) {
            return null;
        }
    }
}
