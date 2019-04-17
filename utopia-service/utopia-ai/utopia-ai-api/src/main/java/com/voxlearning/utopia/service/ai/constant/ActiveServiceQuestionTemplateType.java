package com.voxlearning.utopia.service.ai.constant;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author guangqing
 * @since 2018/10/30
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum  ActiveServiceQuestionTemplateType {
    normal("标准"),
    unknown("未定义");

    private final String desc;
    public static ActiveServiceQuestionTemplateType safeOf(String name) {
        try {
            return ActiveServiceQuestionTemplateType.valueOf(name);
        } catch (Exception e) {
            return unknown;
        }
    }
}
