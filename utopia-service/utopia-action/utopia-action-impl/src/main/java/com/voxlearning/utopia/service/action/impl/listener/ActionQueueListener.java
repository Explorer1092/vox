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

package com.voxlearning.utopia.service.action.impl.listener;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.impl.service.ActionServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * The listener of action queue.
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.queue.action"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.queue.action")
        },
        maxPermits = 64
)
public class ActionQueueListener implements MessageListener {

    @Inject
    private ActionServiceImpl actionService;

    @Override
    public void onMessage(Message message) {
        ActionEvent event = null;
        Object decoded = message.decodeBody();
        if (decoded instanceof String) {
            String messageText = (String) decoded;
            event = JsonUtils.fromJson(messageText, ActionEvent.class);
        }
        if (decoded instanceof ActionEvent) {
            event = (ActionEvent) decoded;
        }
        if (event != null) {
            actionService.handleActionEvent(event);
        }
    }
}
