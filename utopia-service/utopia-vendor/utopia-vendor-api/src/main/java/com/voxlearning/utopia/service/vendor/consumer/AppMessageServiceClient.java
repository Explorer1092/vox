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

package com.voxlearning.utopia.service.vendor.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.vendor.api.AppMessageService;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.entity.AppJpushTimingMessage;
import com.voxlearning.utopia.service.vendor.api.entity.AppUserMessageDynamic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2015/12/22.
 */
public class AppMessageServiceClient implements AppMessageService {

    @ImportService(interfaceClass = AppMessageService.class)
    private AppMessageService remoteReference;

    @Override
    @Deprecated
    public void sendAppJpushMessageByIds(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByIds(content, src, userIds, extInfo);
    }

    @Override
    public void sendAppJpushMessageByIds(String content, AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo) {
        if (StringUtils.isBlank(content) || CollectionUtils.isEmpty(userIds)) {
            return;
        }
        remoteReference.sendAppJpushMessageByIds(content, source, userIds, extInfo);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByIds(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByIds(content, src, userIds, extInfo, sendTimeEpochMilli);
    }

    @Override
    public void sendAppJpushMessageByIds(String content, AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        remoteReference.sendAppJpushMessageByIds(content, source, userIds, extInfo, sendTimeEpochMilli);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByTags(content, src, tags, tagsAnd, extInfo);
    }

    public void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo) {
        if (StringUtils.isBlank(content) || (CollectionUtils.isEmpty(tags)) && CollectionUtils.isEmpty(tagsAnd)) {
            return;
        }
        if (source == null || source == AppMessageSource.UNKNOWN) {
            return;
        }
        remoteReference.sendAppJpushMessageByTags(content, source, tags, tagsAnd, extInfo);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByTags(content, src, tags, tagsAnd, extInfo, sendTimeEpochMilli);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        if (StringUtils.isBlank(content) || (CollectionUtils.isEmpty(tags)) && CollectionUtils.isEmpty(tagsAnd)) {
            return;
        }
        if (source == null || source == AppMessageSource.UNKNOWN) {
            return;
        }

        remoteReference.sendAppJpushMessageByTags(content, source, tags, tagsAnd, extInfo, sendTimeEpochMilli);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime, Long sendTimeEpochMilli) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByTags(content, src, tags, tagsAnd, extInfo, durationTime, sendTimeEpochMilli);
    }

    @Override
    public void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime, Long sendTimeEpochMilli) {
        if (StringUtils.isBlank(content) || (CollectionUtils.isEmpty(tags)) && CollectionUtils.isEmpty(tagsAnd)) {
            return;
        }
        if (source == null || source == AppMessageSource.UNKNOWN) {
            return;
        }

        remoteReference.sendAppJpushMessageByTags(content, source, tags, tagsAnd, extInfo, durationTime, sendTimeEpochMilli);
    }

    @Override
    @Deprecated
    public void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime) {
        AppMessageSource src = AppMessageSource.of(source.name());
        sendAppJpushMessageByTags(content, src, tags, tagsAnd, extInfo, durationTime);
    }


    /**
     * 指定速度发送
     */
    public void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime) {
        if (StringUtils.isBlank(content) || (CollectionUtils.isEmpty(tags) && CollectionUtils.isEmpty(tagsAnd))) {
            return;
        }
        if (source == null || source == AppMessageSource.UNKNOWN) {
            return;
        }
        remoteReference.sendAppJpushMessageByTags(content, source, tags, tagsAnd, extInfo, durationTime);
    }

    @Override
    public void sendAppTimingMessage(List<AppJpushTimingMessage> message) {
        remoteReference.sendAppTimingMessage(message);
    }

    @Override
    public Page<AppJpushTimingMessage> getTimingMessage(Long sendTimeEpochSecond, Pageable pageable) {
        return remoteReference.getTimingMessage(sendTimeEpochSecond, pageable);
    }

    @Override
    public MapMessage saveAppUserDynamicMessage(Collection<AppUserMessageDynamic> messages) {
        return remoteReference.saveAppUserDynamicMessage(messages);
    }

    public void sendAppJpushTimingMessage(List<AppJpushTimingMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        remoteReference.sendAppTimingMessage(messages);
    }

}
