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

package com.voxlearning.utopia.service.wechat.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeUpdater;
import com.voxlearning.utopia.service.wechat.impl.listener.handler.WechatNoticeUpdateHandler;

import javax.inject.Named;

/**
 * @author HuanYin Jia
 * @since 2015/5/25
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.wechat.queue"),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.wechat.queue")
        },
        maxPermits = 64
)
public class WechatQueueListener extends SpringContainerSupport implements MessageListener {

    private WechatNoticeUpdateHandler wechatNoticeUpdateHandler;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        wechatNoticeUpdateHandler = applicationContext.getBean(WechatNoticeUpdateHandler.class);
        if (wechatNoticeUpdateHandler == null) {
            throw new IllegalStateException("No bean type of WechatNoticeUpdateHandler");
        }
    }

    @Override
    public void onMessage(Message message) {
        String messageText = message.getBodyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", messageText);
        }
        try {
            WechatNoticeUpdater updater = WechatNoticeUpdater.deserialize(messageText);
            if (updater != null) {
                wechatNoticeUpdateHandler.handle(updater);
            } else {
                logger.error("handleMessage - Fail to deserialize WechatNoticeUpdater with messageText = {}", messageText);
            }
        } catch (Exception e) {
            logger.error("handleMessage - Excp : {}; messageText = {}", e.getMessage(), messageText);
        }
    }
}