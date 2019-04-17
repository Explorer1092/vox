package com.voxlearning.utopia.service.campaign.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author songtao
 * @since 2016/7/27
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CampaignErrorType {
    DEFAULT("100", "数据异常，重试一下吧"),
    NEED_LOGIN("101", "请重新登录"),
    NO_CLAZZ("102", "班级异常，加入正确的班级后再来学习吧"),
    DUPLICATED_OPERATION("103", "正在处理，请不要重复提交"),
    ACTIVITY_NOT_AVAILABLE("104", "您不能参加这个活动"),
    NOT_AUTHENTICATION("105", "您没有认证"),
    NO_LOTTERY_CHANCE("106", "对不起，您没有抽奖次数了"),
    DATA_ERROR("110", "数据维护中，请过5分钟再重试一下");

    @Getter
    private final String code;
    @Getter
    private final String info;
}
