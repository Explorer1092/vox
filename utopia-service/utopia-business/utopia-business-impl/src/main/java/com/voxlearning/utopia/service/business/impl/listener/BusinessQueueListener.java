/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.listener;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;

import javax.inject.Named;
import java.util.EnumMap;

/**
 * Business queue listener implementation.
 *
 * @author Xiaohai Zhang
 * @since Aug 2, 2016
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.business.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.business.queue"
                )
        },
        maxPermits = 64
)
public class BusinessQueueListener extends ApplicationObjectSupport
        implements MessageListener, InitializingBean {

    private final EnumMap<BusinessEventType, BusinessEventHandler> handlers = new EnumMap<>(BusinessEventType.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), BusinessEventHandler.class, false, true)
                .values().forEach(e -> handlers.put(e.getEventType(), e));
    }

    @Override
    public void onMessage(Message message) {
        String messageText = message.getBodyAsString();
        BusinessEvent event = JsonUtils.fromJson(messageText, BusinessEvent.class);
        if (event == null) return;
        if (event.getType() == null) return;
        handlers.getOrDefault(event.getType(), BusinessEventHandler.NOP).handle(event);
    }

}
