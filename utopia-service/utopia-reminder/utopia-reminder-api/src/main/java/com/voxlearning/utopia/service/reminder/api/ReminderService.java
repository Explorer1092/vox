package com.voxlearning.utopia.service.reminder.api;


import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;

import java.util.concurrent.TimeUnit;

/**
 * @author shiwei.liao
 * @since 2017-5-9
 */
@ServiceVersion(version = "20180801")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface ReminderService extends IPingable {

    @NoResponseWait
    void addUserReminder(Long userId, ReminderPosition position);

    @NoResponseWait
    void addUserNumberReminder(Long userId, ReminderPosition position);

    @NoResponseWait
    void addClazzGroupReminder(Long groupId, ReminderPosition position);

    //按照每个班级给用户加提醒
    @NoResponseWait
    void addUserReminderInGroup(Long userId, Long groupId, ReminderPosition position, String reminderContent);

    @NoResponseWait
    void decrUserReminder(Long userId, ReminderPosition position);

    @NoResponseWait
    void decrUserNumberReminder(Long userId, ReminderPosition position);

    @NoResponseWait
    void decrClazzGroupReminder(Long groupId, ReminderPosition position);

    @NoResponseWait
    void decrUserReminderInGroup(Long userId, Long groupId, ReminderPosition position);

    @NoResponseWait
    void clearUserReminder(Long userId, ReminderPosition position);

    @NoResponseWait
    void clearClazzGroupReminder(Long groupId, ReminderPosition position);

    @NoResponseWait
    void clearUserReminderInGroup(Long userId, Long groupId, ReminderPosition position);
}
