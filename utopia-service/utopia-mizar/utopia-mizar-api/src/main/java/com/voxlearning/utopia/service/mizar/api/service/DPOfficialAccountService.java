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

package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.oa.UserOfficialAccountsRef;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * OfficalAccounts dubbo proxy service.
 *
 * @author yuechen.wang
 * @since 2017-06-13
 */
@ServiceVersion(version = "1.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPOfficialAccountService extends IPingable {

    /**
     * 用户是否关注了公众号
     *
     * @param accountId 公众号的ID
     * @param userId    用户ID
     */
    MapMessage isFollow(Long accountId, Long userId);

    /**
     * 更新公众号关注状态
     *
     * @param accountId 公众号ID
     * @param userId    用户ID
     * @param refStatus 更新的状态， {@link  UserOfficialAccountsRef.Status}
     */
    MapMessage updateFollowStatus(Long accountId, Long userId, String refStatus);

    /**
     * 发送公众号消息
     *
     * @param userIds    用户ID集合
     * @param title      消息标题
     * @param content    消息文案
     * @param linkUrl    跳转链接
     * @param accountKey 公众号Key
     * @param sendPush   是否发送Push消息
     */
    MapMessage sendMessage(Collection<Long> userIds, String title, String content, String linkUrl, String accountKey, Boolean sendPush);

}
