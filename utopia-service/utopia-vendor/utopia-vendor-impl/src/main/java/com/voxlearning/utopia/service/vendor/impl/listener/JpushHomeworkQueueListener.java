package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xinxin
 * @since 28/7/2016
 * 接收homework推送过来的通知,转化成jpush消息推给用户
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.jpush.homework.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.jpush.homework.queue"
                )
        }
)
public class JpushHomeworkQueueListener extends SpringContainerSupport implements MessageListener {
    @Inject
    private JpushHomeworkQueueHandler jpushHomeworkQueueHandler;

    @Override
    public void onMessage(Message message) {
        String messageText = message.getBodyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", messageText);
        }

        jpushHomeworkQueueHandler.handleMessage(messageText);
    }
}
