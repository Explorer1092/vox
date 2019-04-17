package com.voxlearning.utopia.service.reminder.impl.listener;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.mapper.ReminderQueueCommand;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2017-7-25
 */
@Named
@QueueConsumer(destinations = {
        @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.reminder.queue"),
        @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.reminder.queue")
})
public class ReminderQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private ReminderQueueHandler reminderQueueHandler;

    @Override
    public void onMessage(Message message) {
        Object decode = message.decodeBody();
        if (decode instanceof ReminderQueueCommand) {
            ReminderQueueCommand command = (ReminderQueueCommand) decode;
            reminderQueueHandler.handle(command);
            return;
        }
        throw new RuntimeException("unsupport message: " + JsonUtils.toJson(decode));
    }
}
