package com.voxlearning.utopia.service.push.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.push.impl.handler.PushQueueHandler;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by wangshichao on 16/8/24.
 */

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.mi.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.mi.queue"
                )
        }
)
public class MiPushQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject private PushQueueHandler pushQueueHandler;

    @Override
    public void onMessage(Message message) {
        FlightRecorder.closeLog();
        String messageText = message.getBodyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", messageText);
        }
        pushQueueHandler.handleMessage(messageText);
    }
}
