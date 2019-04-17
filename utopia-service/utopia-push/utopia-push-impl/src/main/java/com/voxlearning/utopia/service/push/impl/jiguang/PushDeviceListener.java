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

package com.voxlearning.utopia.service.push.impl.jiguang;

import com.google.common.util.concurrent.RateLimiter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.push.impl.handler.PushQueueHandler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * 极光 device api处理监听
 *
 * @author Wenlong Meng
 * @since Mar 11, 2019
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.push.device.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.push.device.queue")
        },
        maxPermits = 128
)
@Slf4j
public class PushDeviceListener implements MessageListener {

    @Inject private PushQueueHandler pushQueueHandler;
    /**
     * qps
     */
    private RateLimiter rateLimiter = RateLimiter.create(1000);

    @Override
    public void onMessage(Message message) {
        FlightRecorder.closeLog();
        String messageText = message.getBodyAsString();
        log.info("message", messageText);
        Map<String, Object> messageMap = JsonUtils.fromJson(messageText);
        String command = (String)messageMap.get("command");
        String source = (String)messageMap.get("source");
        String data = (String)messageMap.get("data");
        new JGBizTemplate(source, command, null){
            @Override
            protected String url() {
                return super.url() + data;
            }
        }.execute();
        rateLimiter.acquire();
    }
}
