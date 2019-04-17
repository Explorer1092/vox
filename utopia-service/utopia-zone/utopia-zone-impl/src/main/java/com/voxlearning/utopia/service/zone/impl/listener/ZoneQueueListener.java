/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.impl.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import org.springframework.beans.factory.BeanFactoryUtils;

import javax.inject.Named;
import java.util.EnumMap;

/**
 * Clazz zone queue listener implementation.
 *
 * @author Xiaohai Zhang
 * @since Feb 25, 2015
 */
@Named("com.voxlearning.utopia.service.zone.impl.listener.ZoneQueueListener")
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.queue.zone"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.queue.zone")
        },
        maxPermits = 128
)
public class ZoneQueueListener extends SpringContainerSupport implements MessageListener {

    private final EnumMap<ZoneEventType, ZoneEventHandler> handlers = new EnumMap<>(ZoneEventType.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ZoneEventHandler.class, false, true)
                .values().forEach(e -> handlers.put(e.getEventType(), e));

    }

    @Override
    public void onMessage(Message message) {
        try {
            String messageText = message.getBodyAsString();
            ObjectMapper mapper = JsonObjectMapper.OBJECT_MAPPER;
            JsonNode root = mapper.readTree(messageText);

            JsonNode typeNode = root.get("T");

            ZoneEventType messageType;
            try {
                messageType = ZoneEventType.valueOf(typeNode.asText());
            } catch (Exception ex) {
                messageType = ZoneEventType.UNRECOGNIZED;
            }

            // statistics
            ZoneEventStatistics.INSTANCE.increment(messageType);

            if (messageType == ZoneEventType.UNRECOGNIZED) {
                logger.warn("Unrecognized message type '{}', ignore", typeNode.asText());
                return;
            }

            ZoneEventHandler handler = handlers.get(messageType);
            if (handler == null) {
                throw new UnsupportedOperationException();
            }
            handler.handle(root);
            if (logger.isDebugEnabled()) {
                logger.debug("Handled: {}", messageText);
            }
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }

}
