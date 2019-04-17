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

package com.voxlearning.wechat.message;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.support.utils.TokenHelper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Xin Xin
 * @since 10/20/15
 */
@Named
public class MessageSender {
    private static final String SERVICE_MSG_SENDER_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=";
    private static final String TEMPLATE_MSG_SENDER_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";

    @Inject
    private TokenHelper tokenHelper;

    /**
     * 发送客服消息
     *
     * @param msg
     * @return
     */
    public String sendServiceMsg(String msg, WechatType type) {
        String accessToken = tokenHelper.getAccessToken(type);
        String url = SERVICE_MSG_SENDER_URL + accessToken;
        return HttpRequestExecutor.instance(HttpClientType.POOLING).post(url).json(msg).execute().getResponseString();
    }

    /**
     * 发送模板消息
     *
     * @param msg
     * @return
     */
    public String sendTemplateMsg(String msg, WechatType type) {
        String accessToken = tokenHelper.getAccessToken(type);
        String url = TEMPLATE_MSG_SENDER_URL + accessToken;
        return HttpRequestExecutor.instance(HttpClientType.POOLING).post(url).json(msg).execute().getResponseString();
    }
}
