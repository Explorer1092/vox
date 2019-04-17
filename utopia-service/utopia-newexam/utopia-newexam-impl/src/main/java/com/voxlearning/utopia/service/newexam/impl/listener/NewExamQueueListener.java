package com.voxlearning.utopia.service.newexam.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2016/3/8
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.newexam.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.newexam.queue"
                )
        },
        maxPermits = 64
)
public class NewExamQueueListener extends SpringContainerSupport implements MessageListener {
    @Inject
    NewExamQueueHandle newExamQueueHandle;

    @Override
    public void onMessage(Message message) {
        String messageText = message.getBodyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", messageText);
        }
        newExamQueueHandle.handleMessage(messageText);
    }
}
