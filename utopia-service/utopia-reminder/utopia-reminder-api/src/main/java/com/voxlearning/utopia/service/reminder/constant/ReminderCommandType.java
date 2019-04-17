package com.voxlearning.utopia.service.reminder.constant;

/**
 * @author shiwei.liao
 * @since 2017-7-31
 */
public enum ReminderCommandType {
    INCR,
    DECR,
    CLEAR;

    public static ReminderCommandType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
