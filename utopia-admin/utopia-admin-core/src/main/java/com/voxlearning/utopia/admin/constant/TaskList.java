package com.voxlearning.utopia.admin.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by XiaoPeng.Yang on 15-4-8.
 * 需要二次确认的任务列表
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TaskList {
    AutoRewardTeacherByCampusActiveRate("每月根据校园活跃度奖励老师任务"),
    AutoActivityRechargeTeacherTask("运营活动每月自动充话费任务");
    @Getter private final String taskDesc;
}
