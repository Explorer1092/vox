package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum NewHomeworkPublishMessageType {

    assign("布置作业"),
    deleted("删除作业"),
    checked("检查作业"),
    finished("完成作业"),
    corrected("批改作业"),
    adjust("调整作业"),
    confirm("确认作业"),
    comment("评语"),
    shareHomeworkReport("分享作业报告"),
    shareWeekReport("分享周报告"),
    UNKNOWN("未知");

    @Getter
    private final String desc;

    public static NewHomeworkPublishMessageType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
