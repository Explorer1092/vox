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

package com.voxlearning.utopia.service.wechat.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatLoader;
import com.voxlearning.galaxy.service.wechat.api.util.ParentWechatInfoProvider;
import com.voxlearning.utopia.service.wechat.api.DPWechatService;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeUpdater;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sms dubbo proxy service implementation of {@link DPWechatService}.
 *
 * @author yuechen.wang
 * @since 2017-06-13
 */
@Named
@Service(interfaceClass = DPWechatService.class)
@ExposeService(interfaceClass = DPWechatService.class)
public class DPWechatServiceImpl extends SpringContainerSupport implements DPWechatService {
    @Inject
    private WechatServiceImpl wechatService;
    @Inject
    private WechatCodeServiceImpl wechatCodeService;

    @ImportService(interfaceClass = DPWechatLoader.class)
    private DPWechatLoader dpWechatLoader;

    @Override
    public MapMessage sendWechatNotice(String noticeType, Long userId, Map<String, Object> extensionInfo, String wechatType) {
        if (StringUtils.isAnyBlank(noticeType, wechatType) || userId == null || MapUtils.isEmpty(extensionInfo)) {
            return MapMessage.errorMessage("参数错误");
        }

        WechatNoticeProcessorType wechatNoticeProcessorType;
        WechatType type;

        try {
            wechatNoticeProcessorType = WechatNoticeProcessorType.valueOf(noticeType);
            type = WechatType.valueOf(wechatType);
        } catch (Exception ignored) {
            return MapMessage.errorMessage("发送消息类型错误");
        }

        // 重点检查一下sendTime的问题
        if (extensionInfo.containsKey("sendTime")) {
            String dateStr = SafeConverter.toString(extensionInfo.remove("sendTime"));
            if (StringUtils.isNotBlank(dateStr) && !dateStr.matches("^[0-9]{14}$")) {
                return MapMessage.errorMessage("无效的发送时间参数");
            }
            extensionInfo.put("sendTime", DateUtils.stringToDate(dateStr, "yyyyMMddHHmmss"));
        }
        try {
            // 参数校验完毕，发送吧
            wechatService.processWechatNoticeNoWait(wechatNoticeProcessorType, userId, extensionInfo, type);
        } catch (Exception ex) {
            logger.error("Failed send DPWechatNotice, Please check it right now", ex);
            return MapMessage.errorMessage("发送失败：" + ex.getMessage());
        }
        return MapMessage.successMessage("发送成功");
    }

    @Override
    public MapMessage batchSendWechatNotice(String noticeType, List<Long> userIds, Map<String, Object> extensionInfo, String wechatType) {
        if (CollectionUtils.isNotEmpty(userIds)) {
            for (Long userId : userIds) {
                Map<String, Object> map = new HashMap<>();
                map.putAll(extensionInfo);
                sendWechatNotice(noticeType, userId, map, wechatType);
            }
        }
        return MapMessage.successMessage("发送成功");
    }

    @Override
    public MapMessage bindWechatUser(String openid, Long userId) {
        return wechatService.bindUserAndWechat(userId, openid, "17xue", 0);
    }

    @Override
    public MapMessage unbindWechatUser(String openid) {
        return wechatService.unbindUserAndWechat(openid);
    }

    @Override
    public boolean updateNoticeState(String openId, String messageId, WechatNoticeState state, String errorCode) {
        // wechatService.updateNoticeState2(openId, messageId, state, errorCode);
        if (StringUtils.isBlank(openId) || StringUtils.isBlank(messageId)) {
            logger.error("updateNoticeState - Illegal args : openId = {}, messageId = {}", openId, messageId);
            return false;
        }
        WechatNoticeUpdater updater = new WechatNoticeUpdater(openId, messageId, state, errorCode);
        return sendWechatNoticeUpdateMessage(updater);
    }

    @Override
    public MapMessage generateJsApiTicket() {
        String ticket = dpWechatLoader.getJsTicket(ParentWechatInfoProvider.INSTANCE.wechatInfoContext());
        return MapMessage.successMessage().add("ticket", ticket);
    }

    @Override
    public MapMessage sendWechatNoticeByOpenid(String noticeType, String openId, Map<String, Object> extensionInfo, String wechatType) {
        if (StringUtils.isAnyBlank(noticeType, wechatType) || openId == null || MapUtils.isEmpty(extensionInfo)) {
            return MapMessage.errorMessage("参数错误");
        }

        WechatNoticeProcessorType wechatNoticeProcessorType;
        WechatType type;

        try {
            wechatNoticeProcessorType = WechatNoticeProcessorType.valueOf(noticeType);
            type = WechatType.valueOf(wechatType);
        } catch (Exception ignored) {
            return MapMessage.errorMessage("发送消息类型错误");
        }

        // 重点检查一下sendTime的问题
        if (extensionInfo.containsKey("sendTime")) {
            String dateStr = SafeConverter.toString(extensionInfo.remove("sendTime"));
            if (StringUtils.isNotBlank(dateStr) && !dateStr.matches("^[0-9]{14}$")) {
                return MapMessage.errorMessage("无效的发送时间参数");
            }
            extensionInfo.put("sendTime", DateUtils.stringToDate(dateStr, "yyyyMMddHHmmss"));
        }
        try {
            // 参数校验完毕，发送吧
            wechatService.processWechatNoticeByOpenId(wechatNoticeProcessorType, openId, extensionInfo, type);
        } catch (Exception ex) {
            logger.error("Failed send DPWechatNotice, Please check it right now", ex);
            return MapMessage.errorMessage("发送失败：" + ex.getMessage());
        }
        return MapMessage.successMessage("发送成功");
    }

    @Override
    public MapMessage batchSendWechatNoticeByOpenid(String noticeType, List<String> openIds, Map<String, Object> extensionInfo, String wechatType) {
        if (CollectionUtils.isNotEmpty(openIds)) {
            for (String openId : openIds) {
                Map<String, Object> map = new HashMap<>();
                map.putAll(extensionInfo);
                sendWechatNoticeByOpenid(noticeType, openId, map, wechatType);
            }
        }
        return MapMessage.successMessage("发送成功");
    }

    @Override
    public MapMessage generateAccessToken() {
        String token = dpWechatLoader.getAccessToken(ParentWechatInfoProvider.INSTANCE.wechatInfoContext());
        return MapMessage.successMessage().add("token", token);
    }

    private boolean sendWechatNoticeUpdateMessage(WechatNoticeUpdater updater) {
        try {
            Message message = Message.newMessage().withStringBody(updater.serialize());
            wechatService.sendMessage(message);
        } catch (Exception e) {
            logger.error("sendWechatNoticeUpdateMessage - WechatQueueSender send message Excp : {}", e);
            return false;
        }
        return true;
    }

}
