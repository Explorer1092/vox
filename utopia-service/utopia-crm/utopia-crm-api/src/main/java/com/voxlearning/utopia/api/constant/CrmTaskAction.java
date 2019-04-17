package com.voxlearning.utopia.api.constant;

/**
 * @author Jia HuanYin
 * @since 2015/7/8
 */
public enum CrmTaskAction {
    TASK_NEW,
    TASK_FORWARD,
    RECORD_NEW;

    public static CrmTaskAction nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
