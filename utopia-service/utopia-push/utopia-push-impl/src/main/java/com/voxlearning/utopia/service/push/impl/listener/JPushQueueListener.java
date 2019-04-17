/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.push.impl.listener;

import com.voxlearning.alps.config.runtime.ProductProperties;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.push.impl.handler.PushQueueHandler;
import com.voxlearning.utopia.service.push.impl.support.JpushSendRateController;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Jpush queue listener
 * Created by Shuai Huan on 2016/3/7.
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.jpush.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.jpush.queue")
        },
        maxPermits = 128
)
public class JPushQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject private PushQueueHandler pushQueueHandler;

    @Inject private JpushSendRateController jpushSendRateController;

    @Override
    public void onMessage(Message message) {
        FlightRecorder.closeLog();
        String messageText = message.getBodyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", messageText);
        }
        String value = ProductProperties.getProperty("product.config.use.jpush.send.rate.controller", "false");
        if (SafeConverter.toBoolean(value)) {
            jpushSendRateController.acquire();
        }

        pushQueueHandler.handleMessage(messageText);

    }
}
