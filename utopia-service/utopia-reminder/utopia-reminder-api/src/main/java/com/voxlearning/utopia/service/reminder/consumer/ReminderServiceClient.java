package com.voxlearning.utopia.service.reminder.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.reminder.api.ReminderService;
import lombok.Getter;

/**
 * @author shiwei.liao
 * @since 2017-5-10
 */
public class ReminderServiceClient {

    @Getter
    @ImportService(interfaceClass = ReminderService.class)
    private ReminderService reminderService;
}
