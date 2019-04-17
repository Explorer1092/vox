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

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.entity.AppUserMessageDynamic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Vendor dubbo proxy service.
 */
@ServiceVersion(version = "2016.08.19")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPVendorService {

    // ========================================================================
    // com.voxlearning.utopia.service.vendor.api.AppMessageService
    // ========================================================================

    void sendAppJpushMessageByIds(String content, AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo);

    void sendAppJpushMessageByIds(String content, String source, List<Long> userIds, Map<String, Object> extInfo);


    void sendAppJpushMessageByIds(String content, AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo, Long sendTimeEpochMilli);

    void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo);

    void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime);

    void sendAppJpushMessageByTags(String content, String source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime);

    MapMessage saveAppUserDynamicMessage(Collection<AppUserMessageDynamic> messages);

    void sendClassmatesUsageToParentFairyland(Long clazzId, Long studentId, String ak, String content);

    // 自学应用获取用户信息
    Map<String, Object> getUserInfo(String appKey, String sessionKey);

    //保存一起学的tag
    Boolean setUserYiQiXuePushTag(Long userId, Set<String> tags);

    Set<String> loadUserYiQiXuePushTag(Long userId);

    MapMessage isValidRequest(String appKey, String sessionKey, Map<String, String> request, String sig);

    MapMessage loadVendorApps(String appKey);

    MapMessage updateUserAppSessionKey(String appKey, Long userId);

    MapMessage loadSessionKey(String appKey, Long userId);



}
