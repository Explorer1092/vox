package com.voxlearning.utopia.service.push.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.push.impl.handler.JpushRetryQueueHandler;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2017-12-21
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK,config = "primary",queue = "yiqixue.pushRetry.queue"),
                @QueueDestination(system = QueueSystem.KFK,config = "main-backup",queue = "yiqixue.pushRetry.queue")
        }
)
public class JpushYiQiXueRetryQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private JpushRetryQueueHandler jpushRetryQueueHandler;
    @Override
    public void onMessage(Message message) {
        FlightRecorder.closeLog();
        String messageText = message.getBodyAsString();
        if(logger.isDebugEnabled()){
            logger.debug("Message received: {}", messageText);
        }
        jpushRetryQueueHandler.handleMessage(messageText);
    }
}
