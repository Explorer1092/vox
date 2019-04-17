package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

/**
 * @author guoqiang.li
 * @since 2016/4/19
 */
public enum HomeworkStatus {
    Checked("已检查"),
    UnChecked("待检查");

    @Getter
    private final String desc;

    HomeworkStatus(String desc) {
        this.desc = desc;
    }

    public static HomeworkStatus of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
