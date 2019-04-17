package com.voxlearning.utopia.api.constant;

import lombok.Getter;

@Getter
public enum CrmTaskStatus {
    NEW("新建"),
    UNFOLLOW("未跟进"),
    FOLLOWING("待跟进"),
    UNFINISH("未完成"),
    FINISHED("已完成");

    public final String value;

    CrmTaskStatus(String value) {
        this.value = value;
    }

    public static CrmTaskStatus nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}

