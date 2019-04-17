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

package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.entity.AppJpushTimingMessage;
import com.voxlearning.utopia.service.vendor.api.entity.AppUserMessageDynamic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author shiwei.liao
 * @since 2015/12/22.
 */
@ServiceVersion(version = "1.2.DEV")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AppMessageService extends IPingable {

    // ========================================================================
    // 目前中学端调用
    // ========================================================================
    @Deprecated
    void sendAppJpushMessageByIds(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo);

    // ========================================================================
    // 目前中学端调用  小学也用了
    // ========================================================================
    @Deprecated
    void sendAppJpushMessageByIds(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo, Long sendTimeEpochMilli);

    // ========================================================================
    // 目前中学端调用
    // ========================================================================
    @Deprecated
    void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo);

    /**
     * 带定时发送功能
     *
     * @param content
     * @param source
     * @param tags
     * @param tagsAnd
     * @param extInfo
     * @param sendTimeEpochMilli 延迟时间
     * @deprecated 此方法未指定发送时送，对于大批量的情况，会导致数据库压力过大
     */
    @Deprecated
    void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Long sendTimeEpochMilli);

    /**
     * 带定时发送功能
     *
     * @param content
     * @param source
     * @param tags
     * @param tagsAnd
     * @param extInfo
     * @param sendTimeEpochMilli 延迟时间
     * @param durationTime       发送时长
     */
    @Deprecated
    void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime, Long sendTimeEpochMilli);

    // ========================================================================
    // 目前中学端调用
    // ========================================================================
    @Deprecated
    void sendAppJpushMessageByTags(String content, com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime);

    void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime);

    void sendAppJpushMessageByIds(String content, AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo);

    void sendAppJpushMessageByIds(String content, AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo, Long sendTimeEpochMilli);

    void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo);

    @Deprecated
    void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Long sendTimeEpochMilli);

    void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime, Long sendTimeEpochMilli);

    void sendAppTimingMessage(List<AppJpushTimingMessage> message);

    Page<AppJpushTimingMessage> getTimingMessage(Long sendTimeEpochSecond, Pageable pageable);

    // ========================================================================
    // 目前中学端调用
    // 平台端禁止再调用！！！！！
    // 平台端禁止再调用！！！！！
    // 平台端禁止再调用！！！！！
    // 睁开你们的钛金眼看清楚！！
    // ========================================================================
    @Deprecated
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    MapMessage saveAppUserDynamicMessage(Collection<AppUserMessageDynamic> messages);
}
