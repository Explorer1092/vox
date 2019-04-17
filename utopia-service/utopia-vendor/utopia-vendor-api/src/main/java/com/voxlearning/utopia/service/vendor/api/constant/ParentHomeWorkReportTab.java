package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by jiang wei on 2017/2/9.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ParentHomeWorkReportTab {
    NEW_HOMEWORK_TAB(0, "新作业"),
    FINISH_HOMEWORK_TAB(1, "已完成"),
    ;


    private final Integer tabType;
    private final String desc;
}
