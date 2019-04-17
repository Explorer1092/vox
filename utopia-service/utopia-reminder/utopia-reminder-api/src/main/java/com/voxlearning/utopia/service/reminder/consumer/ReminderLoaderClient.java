package com.voxlearning.utopia.service.reminder.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.reminder.api.ReminderLoader;
import lombok.Getter;

/**
 * @author shiwei.liao
 * @since 2017-5-10
 */
public class ReminderLoaderClient {

    @Getter
    @ImportService(interfaceClass = ReminderLoader.class)
    private ReminderLoader reminderLoader;
}
