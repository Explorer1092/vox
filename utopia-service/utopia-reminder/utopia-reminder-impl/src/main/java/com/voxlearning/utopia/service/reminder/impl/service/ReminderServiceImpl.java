package com.voxlearning.utopia.service.reminder.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.reminder.api.ReminderService;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.reminder.constant.ReminderTarget;
import com.voxlearning.utopia.service.reminder.impl.support.ReminderCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2017-5-9
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = ReminderService.class, version = @ServiceVersion(version = "20170724")),
        @ExposeService(interfaceClass = ReminderService.class, version = @ServiceVersion(version = "20180801"))
})
public class ReminderServiceImpl implements ReminderService {
    @Inject
    private ReminderCacheSystem reminderCacheSystem;

    @Override
    public void addUserReminder(Long userId, ReminderPosition position) {
        if (userId == null || position == null) {
            return;
        }
        reminderCacheSystem.getReminderCache().incr(ReminderTarget.USER, position, userId.toString(), false);
    }

    @Override
    public void addUserNumberReminder(Long userId, ReminderPosition position) {
        if (userId == null || position == null) {
            return;
        }
        reminderCacheSystem.getReminderCache().incr(ReminderTarget.USER, position, userId.toString(), true);
    }

    @Override
    public void addClazzGroupReminder(Long groupId, ReminderPosition position) {
        if (groupId == null || position == null) {
            return;
        }
        reminderCacheSystem.getReminderCache().incr(ReminderTarget.CLAZZ_GROUP, position, groupId.toString(), false);
    }


    @Override
    public void addUserReminderInGroup(Long userId, Long groupId, ReminderPosition position, String reminderContent) {
        if (userId == null || groupId == null || position == null) {
            return;
        }
        String targetId = "UID=" + userId + ",GID=" + groupId;
        reminderCacheSystem.getReminderCache().incr(ReminderTarget.USER_IN_GROUP, position, targetId, reminderContent);
    }

    @Override
    public void decrUserReminder(Long userId, ReminderPosition position) {
        if (userId == null || position == null) {
            return;
        }
        reminderCacheSystem.getReminderCache().decr(ReminderTarget.USER, position, userId.toString(), false);
    }

    @Override
    public void decrUserNumberReminder(Long userId, ReminderPosition position) {
        if (userId == null || position == null) {
            return;
        }
        reminderCacheSystem.getReminderCache().decr(ReminderTarget.USER, position, userId.toString(), true);
    }

    @Override
    public void decrClazzGroupReminder(Long groupId, ReminderPosition position) {
        if (groupId == null || position == null) {
            return;
        }
        reminderCacheSystem.getReminderCache().decr(ReminderTarget.CLAZZ_GROUP, position, groupId.toString(), false);
    }

    @Override
    public void decrUserReminderInGroup(Long userId, Long groupId, ReminderPosition position) {
        if (userId == null || groupId == null || position == null) {
            return;
        }
        String targetId = "UID=" + userId + ",GID=" + groupId;
        reminderCacheSystem.getReminderCache().decr(ReminderTarget.USER_IN_GROUP, position, targetId, false);
    }

    @Override
    public void clearUserReminder(Long userId, ReminderPosition position) {
        if (userId == null || position == null) {
            return;
        }
        reminderCacheSystem.getReminderCache().delete(ReminderTarget.USER, position, userId.toString());
    }

    @Override
    public void clearClazzGroupReminder(Long groupId, ReminderPosition position) {
        if (groupId == null || position == null) {
            return;
        }
        reminderCacheSystem.getReminderCache().delete(ReminderTarget.CLAZZ_GROUP, position, groupId.toString());
    }

    @Override
    public void clearUserReminderInGroup(Long userId, Long groupId, ReminderPosition position) {
        if (userId == null || groupId == null || position == null) {
            return;
        }
        String targetId = "UID=" + userId + ",GID=" + groupId;
        reminderCacheSystem.getReminderCache().delete(ReminderTarget.USER_IN_GROUP, position, targetId);
    }
}
