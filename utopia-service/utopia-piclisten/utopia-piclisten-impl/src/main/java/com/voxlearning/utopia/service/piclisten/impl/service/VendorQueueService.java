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

package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class VendorQueueService extends SpringContainerSupport {


    @Getter
    @AlpsQueueProducer(queue = "utopia.vendor.queue")
    private MessageProducer producer;

    public void sendHttpNotify(String appKey, String targetUrl, Map<String, Object> params) {
        Map<String, Object> sendMessage = new HashMap<>();
        sendMessage.put("notifyId", "");
        sendMessage.put("appKey", appKey);
        sendMessage.put("targetUrl", targetUrl);
        sendMessage.put("params", params);
        String messageText = JsonUtils.toJson(sendMessage);
        producer.produce(Message.newMessage().withStringBody(messageText));
        MapMessage.successMessage();
    }
}
