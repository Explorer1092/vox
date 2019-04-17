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

package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.entity.AppJpushTimingMessage;
import com.voxlearning.utopia.service.push.api.support.PushContext;
import com.voxlearning.utopia.service.vendor.api.AppMessageService;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.entity.AppUserMessageDynamic;
import com.voxlearning.utopia.service.vendor.impl.dao.AppJpushTimingMessageDao;
import com.voxlearning.utopia.service.vendor.impl.push.AppMessagePushProcessor;
import com.voxlearning.utopia.service.vendor.impl.push.InternalPushService;
import com.voxlearning.utopia.service.vendor.impl.push.PushProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author shiwei.liao
 * @since 2015/12/22.
 */
@Named("vendorAppMessageService")
@Service(interfaceClass = AppMessageService.class)
@ExposeServices({
        @ExposeService(interfaceClass = AppMessageService.class, version = @ServiceVersion(version = "1.2.DEV")),
        @ExposeService(interfaceClass = AppMessageService.class, version = @ServiceVersion(version = "1.1.DEV"))
})
public class AppMessageServiceImpl extends SpringContainerSupport implements AppMessageService {

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Inject
    private AppJpushTimingMessageDao appJpushTimingMessageDao;
    @Inject
    private AppMessagePushProcessor appMessagePushProcessor;
    @Inject
    private InternalPushService internalPushService;
    @Inject
    private PushProducer pushProducer;

    @Override
    public void sendAppJpushMessageByIds(String content, AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo) {
        if (StringUtils.isBlank(content) || CollectionUtils.isEmpty(userIds)) {
            return;
        }

        appMessagePushProcessor.processUid(source, content, userIds, extInfo);
    }

    @Override
    public void sendAppJpushMessageByIds(String content, AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        if (StringUtils.isBlank(content) || CollectionUtils.isEmpty(userIds)) {
            return;
        }

        appMessagePushProcessor.processUid(source, content, userIds, extInfo, sendTimeEpochMilli);
    }

    @Override
    public void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo) {
        if (StringUtils.isBlank(content) || (CollectionUtils.isEmpty(tags)) && CollectionUtils.isEmpty(tagsAnd)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(tagsAnd) && tagsAnd.stream().allMatch(p -> StringUtils.equalsIgnoreCase(p, JpushUserTag.REFACTOR_PUSH_VERSION.tag))) {
            if (CollectionUtils.isEmpty(tags)) {
                return;
            } else {
                tagsAnd.addAll(tags);
            }
        }

        if (source == null || source == AppMessageSource.UNKNOWN) {
            return;
        }
        //不需要定速发送时，durationTime设置为0
        appMessagePushProcessor.processTag(source, content, tags, tagsAnd, extInfo, 0);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        if (StringUtils.isBlank(content) || (CollectionUtils.isEmpty(tags)) && CollectionUtils.isEmpty(tagsAnd)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(tagsAnd) && tagsAnd.stream().allMatch(p -> StringUtils.equalsIgnoreCase(p, JpushUserTag.REFACTOR_PUSH_VERSION.tag))) {
            if (CollectionUtils.isEmpty(tags)) {
                return;
            } else {
                tagsAnd.addAll(tags);
            }
        }
        if (source == null || source == AppMessageSource.UNKNOWN) {
            return;
        }

        appMessagePushProcessor.processTag(source, content, tags, tagsAnd, extInfo, 0, sendTimeEpochMilli);
    }

    @Override
    public void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime, Long sendTimeEpochMilli) {
        if (StringUtils.isBlank(content) || (CollectionUtils.isEmpty(tags)) && CollectionUtils.isEmpty(tagsAnd)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(tagsAnd) && tagsAnd.stream().allMatch(p -> StringUtils.equalsIgnoreCase(p, JpushUserTag.REFACTOR_PUSH_VERSION.tag))) {
            if (CollectionUtils.isEmpty(tags)) {
                return;
            } else {
                tagsAnd.addAll(tags);
            }
        }
        if (source == null || source == AppMessageSource.UNKNOWN) {
            return;
        }

        appMessagePushProcessor.processTag(source, content, tags, tagsAnd, extInfo, durationTime, sendTimeEpochMilli);
    }


    @Override
    @Deprecated
    public void sendAppJpushMessageByIds(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByIds(content, src, userIds, extInfo);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByIds(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByIds(content, src, userIds, extInfo, sendTimeEpochMilli);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByTags(content, src, tags, tagsAnd, extInfo);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByTags(content, src, tags, tagsAnd, extInfo, sendTimeEpochMilli);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime, Long sendTimeEpochMilli) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByTags(content, src, tags, tagsAnd, extInfo, durationTime, sendTimeEpochMilli);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByTags(content, src, tags, tagsAnd, extInfo, durationTime);
    }

    @Override
    public void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime) {
        if (StringUtils.isBlank(content) || (CollectionUtils.isEmpty(tags) && CollectionUtils.isEmpty(tagsAnd))) {
            return;
        }
        if (CollectionUtils.isNotEmpty(tagsAnd) && tagsAnd.stream().allMatch(p -> StringUtils.equalsIgnoreCase(p, JpushUserTag.REFACTOR_PUSH_VERSION.tag))) {
            if (CollectionUtils.isEmpty(tags)) {
                return;
            } else {
                tagsAnd.addAll(tags);
            }
        }
        if (source == null || source == AppMessageSource.UNKNOWN) {
            return;
        }

        appMessagePushProcessor.processTag(source, content, tags, tagsAnd, extInfo, durationTime);
    }

    @Override
    public void sendAppTimingMessage(List<AppJpushTimingMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }


        messages.forEach(m -> {
            AppMessageSource source = AppMessageSource.of(m.getMessageSource());
            if (source != AppMessageSource.UNKNOWN) {
                if (isUmeng(m)) {
                    PushContext context = JsonUtils.fromJson(m.getNotify(), PushContext.class);
                    pushProducer.produce(context);
                } else {
                    internalPushService.sendJpushNotify(JsonUtils.fromJson(m.getNotify()));
                }
            }
        });
    }

    @Override
    public Page<AppJpushTimingMessage> getTimingMessage(Long sendTimeEpochSecond, Pageable pageable) {
        if (null == sendTimeEpochSecond || 0 == sendTimeEpochSecond || null == pageable) {
            return null;
        }

        return appJpushTimingMessageDao.getTimingMessage(sendTimeEpochSecond, pageable);
    }

    @Override
    public MapMessage saveAppUserDynamicMessage(Collection<AppUserMessageDynamic> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return MapMessage.successMessage();
        }
        // 这个方法纯粹是中转的实现，保留是为了兼容中学端调用
        // use ASYNC mode for we don't need response.
        messages.stream()
                .filter(Objects::nonNull)
                .map(s -> {
                    AppMessage t = new AppMessage();
                    t.setId(s.getId());
                    t.setUserId(s.getUserId());
                    t.setMessageType(s.getMessageType());
                    t.setTitle(s.getTitle());
                    t.setContent(s.getContent());
                    t.setImageUrl(s.getImageUrl());
                    t.setLinkUrl(s.getLinkUrl());
                    t.setLinkType(s.getLinkType());
                    t.setExtInfo(s.getExtInfo());
                    t.setIsTop(s.getIsTop());
                    t.setTopEndTime(s.getTopEndTime());
                    t.setViewed(s.getViewed());
                    t.setExpiredTime(s.getExpiredTime());
                    t.setPopupTitle(s.getPopupTitle());
                    t.setAppTagMsgId(s.getAppTagMsgId());
                    t.setCreateTime(s.getCreateTime());
                    return t;
                })
                .forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        return MapMessage.successMessage();
    }

    private boolean isUmeng(AppJpushTimingMessage message) {
        PushContext context = JsonUtils.fromJson(message.getNotify(), PushContext.class);
        return null != context && null != context.getTargetType() && null != context.getAliases();
    }
}
