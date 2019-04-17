/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.vendor.impl.queue.VendorQueueProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal vendor queue sender (simulation).
 * No really send message text into rabbit mq, use thread pool instead.
 *
 * @author Xiaohai Zhang
 * @since Feb 10, 2015
 */
@Named
public class InternalVendorQueueSender extends SpringContainerSupport {

    @Inject private VendorQueueProducer vendorQueueProducer;

    public void sendHttpNotify(final Long notifyId, final String appKey, final String targetUrl, final Map<String, Object> params) {
        Map<String, Object> sendMessage = new HashMap<>();
        sendMessage.put("notifyId", String.valueOf(notifyId));
        sendMessage.put("appKey", appKey);
        sendMessage.put("targetUrl", targetUrl);
        sendMessage.put("params", params);
        String messageText = JsonUtils.toJson(sendMessage);
        vendorQueueProducer.getProducer().produce(Message.newMessage().withStringBody(messageText));
    }
}
