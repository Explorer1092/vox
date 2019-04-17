package com.voxlearning.utopia.api.constant;

import lombok.Getter;

/**
 * @author Jia HuanYin
 * @since 2015/8/18
 */
@Getter
public enum CrmMeetingSupportLevel {
    HARD_RECOMMEND("力推"),
    RECOMMEND("推荐"),
    RED_HEAD_DOCUMENT("红头文件");

    public final String value;

    CrmMeetingSupportLevel(String value) {
        this.value = value;
    }

    public static CrmMeetingSupportLevel nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
