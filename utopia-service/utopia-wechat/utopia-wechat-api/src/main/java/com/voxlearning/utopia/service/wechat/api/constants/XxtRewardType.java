package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by tanguohong on 14-10-29.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum XxtRewardType {
    WEEK_FIRST_TIME(1, "每周第一次发送消息奖励"),
    NORM(2, "标准发送消息奖励"),
    BIND_WECHAT(3, "绑定微信奖励");

    private final int value;
    private final String description;
}
