package com.voxlearning.utopia.service.ai.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author guangqing
 * @since 2019/3/19
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum PageViewType {
    STUDY_INFORMATION("study_information"),
    RENEW("renew"),//续费提醒
    REPORT("report"),//定级报告
    GROUP_BUY("group_buy"),//拼团广告页

    unknown("未定义");

    private final String desc;
    public static PageViewType safeOf(String name) {
        try {
            return PageViewType.valueOf(name);
        } catch (Exception e) {
            return unknown;
        }
    }
}
