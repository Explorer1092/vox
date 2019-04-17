package com.voxlearning.utopia.service.push.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.push.impl.handler.JpushRetryQueueHandler;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Jpush retryQueue listener
 * Created by malong on 2016/4/25.
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.pushRetry.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.pushRetry.queue"
                )
        }
)
public class JpushRetryQueueListener extends SpringContainerSupport implements MessageListener {
    @Inject private JpushRetryQueueHandler jpushRetryQueueHandler;

    @Override
    public void onMessage(Message message) {
        String messageText = message.getBodyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", messageText);
        }
        jpushRetryQueueHandler.handleMessage(messageText);
    }
}
