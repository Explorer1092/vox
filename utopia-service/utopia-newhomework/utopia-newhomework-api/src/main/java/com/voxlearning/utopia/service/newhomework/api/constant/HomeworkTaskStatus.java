package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author guoqiang.li
 * @since 2017/4/17
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum HomeworkTaskStatus {
    UNFINISHED("去完成", "#ffc22e"),
    FINISHED("奖励待领取", "#ff6802"),
    REWARDED("奖励已领取", "#c5cdd1");

    @Getter private final String description;
    @Getter private final String color;
}
