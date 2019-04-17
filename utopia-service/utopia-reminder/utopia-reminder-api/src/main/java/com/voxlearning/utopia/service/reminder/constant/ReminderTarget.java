package com.voxlearning.utopia.service.reminder.constant;

/**
 * @author shiwei.liao
 * @since 2017-5-10
 */
public enum ReminderTarget {
    USER,
    CLAZZ_GROUP,
    USER_IN_GROUP;

    public static ReminderTarget of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
