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

package com.voxlearning.utopia.service.afenti.impl.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.afenti.api.annotations.AfentiQueueMessageTypeIdentification;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQueueMessageType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static com.voxlearning.alps.lang.mapper.json.JsonObjectMapper.OBJECT_MAPPER;

/**
 * @author tanguohong
 * @since 2016/5/19
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.afenti.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.afenti.queue")
        },
        maxPermits = 64
)
public class AfentiQueueListener extends SpringContainerSupport implements MessageListener {
    private final Map<AfentiQueueMessageType, AfentiQueueMessageHandler> handlers;

    public AfentiQueueListener() {
        this.handlers = new HashMap<>();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        // 获取所有handler
        Map<String, AfentiQueueMessageHandler> beans = applicationContext.getBeansOfType(AfentiQueueMessageHandler.class);
        for (AfentiQueueMessageHandler handler : beans.values()) {
            AfentiQueueMessageTypeIdentification annotation = handler.getClass().getAnnotation(AfentiQueueMessageTypeIdentification.class);
            if (null == annotation) {
                throw new IllegalStateException("No @AfentiQueueMessageTypeIdentification presented on "
                        + handler.getClass().getName());
            }
            AfentiQueueMessageType type = annotation.value();
            if (handlers.containsKey(type)) {
                throw new IllegalStateException("Duplicated handler type " + type);
            }
            handlers.put(type, handler);
        }
    }

    @Override
    public void onMessage(Message message) {
        String body = message.getBodyAsString();
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(body);
        } catch (Exception ex) {
            logger.warn("Not available JSON object: {}", body);
            return;
        }
        if (root == null) {
            logger.warn("Not available JSON object: {}", body);
            return;
        }

        AfentiQueueMessageType type = null;
        JsonNode T = root.get("T");
        if (T != null) {
            type = AfentiQueueMessageType.safeParse(T.asText());
        }
        if (type == null) {
            logger.warn("Unrecognized AfentiQueueMessageType: {}", body);
            return;
        }
        AfentiQueueMessageHandler handler = handlers.get(type);
        if (handler == null) {
            logger.warn("Unrecognized AfentiQueueMessageType: {}", type);
            return;
        }
        try {
            ObjectMapper mapper = OBJECT_MAPPER;
            handler.handle(mapper, root);
        } catch (Exception ex) {
            logger.warn("Failed to handle message", ex);
        }
    }
}
