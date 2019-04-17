package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author shiwe.liao
 * @since 2016-9-12
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.newhomework.vendor.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.newhomework.vendor.queue"
                )
        }
)
public class NewHomeworkVendorMessageQueueListener extends SpringContainerSupport implements MessageListener {
    @Inject
    private NewHomeworkVendorMessageQueueHandler newHomeworkVendorMessageQueueHandler;


    @Override
    public void onMessage(Message message) {
        String messageText = message.getBodyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", messageText);
        }
        newHomeworkVendorMessageQueueHandler.handleMessage(messageText);
    }
}
