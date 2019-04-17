package com.voxlearning.utopia.service.reminder.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.reminder.api.mapper.ReminderContext;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author shiwei.liao
 * @since 2017-5-10
 */
@ServiceVersion(version = "20170726")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ReminderLoader extends IPingable {

    @Idempotent
    ReminderContext loadUserReminder(Long userId, ReminderPosition position);

    @Idempotent
    Map<Long, ReminderContext> loadUsersReminder(Collection<Long> userIds, ReminderPosition position);

    @Idempotent
    ReminderContext loadClazzGroupReminder(Long groupId, ReminderPosition position);

    @Idempotent
    Map<Long, ReminderContext> loadClazzGroupsReminder(Collection<Long> groupIds, ReminderPosition position);

    @Idempotent
    Map<Long, ReminderContext> loadUserGroupReminder(Long userId, Collection<Long> groupIds, ReminderPosition position);

    @Idempotent
    Map<ReminderPosition, ReminderContext> loadUserReminder(Long userId, Collection<ReminderPosition> positions);
}
