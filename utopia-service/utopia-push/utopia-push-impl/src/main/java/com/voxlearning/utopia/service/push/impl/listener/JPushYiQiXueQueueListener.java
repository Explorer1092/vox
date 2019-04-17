package com.voxlearning.utopia.service.push.impl.listener;

import com.voxlearning.alps.config.runtime.ProductProperties;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.push.impl.handler.PushQueueHandler;
import com.voxlearning.utopia.service.push.impl.support.JpushYiQiXueRateController;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2017-12-21
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "yiqixue.jpush.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "yiqixue.jpush.queue")
        },
        maxPermits = 128
)
public class JPushYiQiXueQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private PushQueueHandler pushQueueHandler;

    @Inject
    private JpushYiQiXueRateController jpushYiQiXueRateController;

    @Override
    public void onMessage(Message message) {
        FlightRecorder.closeLog();
        String messageText = message.getBodyAsString();
        if(logger.isDebugEnabled()){
            logger.debug("Message received: {}", messageText);
        }
        String value = ProductProperties.getProperty("product.config.use.jpush.send.rate.controller", "false");
        if(SafeConverter.toBoolean(value)){
            jpushYiQiXueRateController.acquire();
        }
        pushQueueHandler.handleMessage(messageText);
    }
}
