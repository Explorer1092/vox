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

package com.voxlearning.utopia.service.vendor.consumer.support;

import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.vendor.api.VendorService;
import com.voxlearning.utopia.service.vendor.api.constant.VendorNotifyChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Helper for sending vendor notify.
 *
 * @author Xiaohai Zhang
 * @since Feb 10, 2015
 */
public class VendorNotifyCreator {
    private final VendorService vendorService;

    public VendorNotifyCreator(VendorService vendorService) {
        this.vendorService = Objects.requireNonNull(vendorService);
    }

    private VendorNotifyChannel channel;
    private String notifyId;
    private String appKey;                  // channel为JPSUH时自动设置为VendorConstants.JPUSH_ADVENTURE_APP_KEY
    private String targetUrl;
    private Map<String, Object> params;     // channel为HTTP时使用
    private String jsonContent;             // channel为JPUSH时使用
    private String auth;                    // 认证信息

    public VendorNotifyCreator channel(VendorNotifyChannel channel) {
        this.channel = channel;
        return this;
    }

    public VendorNotifyCreator notifyId(String notifyId) {
        this.notifyId = notifyId;
        return this;
    }

    public VendorNotifyCreator appKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public VendorNotifyCreator targetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
        return this;
    }

    public VendorNotifyCreator params(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public VendorNotifyCreator jsonContent(String jsonContent) {
        this.jsonContent = jsonContent;
        return this;
    }

    public VendorNotifyCreator auth(String auth) {
        this.auth = auth;
        return this;
    }

    public void send() {
        if (notifyId == null || channel == null || targetUrl == null) {
            return;
        }
        String messageText;
        switch (channel) {
            case HTTP: {
                Map<String, Object> sendMessage = new HashMap<>();
                sendMessage.put("notifyId", notifyId);
                sendMessage.put("appKey", appKey);
                sendMessage.put("targetUrl", targetUrl);
                sendMessage.put("params", params);
                messageText = JsonStringSerializer.getInstance().serialize(sendMessage);
                vendorService.sendVendorMessage(Message.newMessage().withStringBody(messageText));
                break;
            }
            case JPUSH: {
                Map<String, Object> sendMessage = new HashMap<>();
                sendMessage.put("jpush", "jpush");
                sendMessage.put("notifyId", notifyId);
                sendMessage.put("appKey", appKey);
                sendMessage.put("targetUrl", targetUrl);
                sendMessage.put("params", jsonContent);
                sendMessage.put("authorization", auth);
                messageText = JsonStringSerializer.getInstance().serialize(sendMessage);
                vendorService.sendJpushMessage(Message.newMessage().withStringBody(messageText));
                break;
            }
            default: {
                throw new UnsupportedOperationException();
            }
        }
    }
}
