package com.voxlearning.utopia.service.reminder.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.reminder.api.DPReminderService;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.reminder.constant.ReminderTarget;
import com.voxlearning.utopia.service.reminder.impl.support.ReminderCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2017-5-23
 */
@Named
@ExposeService(interfaceClass = DPReminderService.class)
public class DPReminderServiceImpl implements DPReminderService {
    @Inject
    private ReminderCacheSystem reminderCacheSystem;

    @Override
    public Boolean addLiveTabReminderToUser(Long userId) {
        if (userId == null) {
            return Boolean.FALSE;
        }
        return reminderCacheSystem.getReminderCache().incr(ReminderTarget.USER, ReminderPosition.PARENT_APP_TAB_LIVE, userId.toString(), false);
    }
}
