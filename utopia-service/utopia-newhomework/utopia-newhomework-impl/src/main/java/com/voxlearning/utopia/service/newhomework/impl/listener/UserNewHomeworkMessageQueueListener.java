package com.voxlearning.utopia.service.newhomework.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2016-10-13
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.user.newhomework.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.user.newhomework.queue")
        }
)
public class UserNewHomeworkMessageQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private UserNewHomeworkMessageQueueHandler userNewHomeworkMessageQueueHandler;

    @Override
    public void onMessage(Message message) {
        String messageText = message.getBodyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", messageText);
        }
        userNewHomeworkMessageQueueHandler.handleMessage(messageText);
    }
}
