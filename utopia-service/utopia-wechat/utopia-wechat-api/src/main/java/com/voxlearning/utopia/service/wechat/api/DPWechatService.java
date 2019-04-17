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

package com.voxlearning.utopia.service.wechat.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WeChat dubbo proxy service.
 *
 * @author yuechen.wang
 * @since 2017-06-13
 */
@ServiceVersion(version = "1.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPWechatService extends IPingable {

    /**
     * 发送微信模板消息
     *
     * @param noticeType    运营消息类型 {@link WechatNoticeProcessorType}
     * @param userId        接收用户ID
     * @param extensionInfo 模板消息内容
     * @param wechatType    微信账号类型 {@link WechatType}
     */
    MapMessage sendWechatNotice(String noticeType, Long userId, Map<String, Object> extensionInfo, String wechatType);

    MapMessage batchSendWechatNotice(String noticeType, List<Long> userIds, Map<String, Object> extensionInfo, String wechatType);

    MapMessage bindWechatUser(String openid, Long userId);

    MapMessage unbindWechatUser(String openid);

    boolean updateNoticeState(String openId, String messageId, WechatNoticeState state, String errorCode);

    MapMessage generateJsApiTicket();

    // 给一起学那边提供的接口，
    MapMessage sendWechatNoticeByOpenid(String noticeType, String openId, Map<String, Object> extensionInfo, String wechatType);

    MapMessage batchSendWechatNoticeByOpenid(String noticeType, List<String> openIds, Map<String, Object> extensionInfo, String wechatType);

    // 给一起学提供的access_token
    MapMessage generateAccessToken();
}
