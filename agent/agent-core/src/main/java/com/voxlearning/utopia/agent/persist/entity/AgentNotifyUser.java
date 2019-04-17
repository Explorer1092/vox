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

package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Notify User Entity
 * Created by Shuai.Huan on 2014/7/21.
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_NOTIFY_USER")
@UtopiaCacheExpiration
public class AgentNotifyUser extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 2169025113244531303L;

    @UtopiaSqlColumn Long notifyId;                 // 通知ID
    @UtopiaSqlColumn Long userId;                   // 用户（通知接收者）ID
    @UtopiaSqlColumn AgentNotifyType notifyType;    // 通知类型
    @UtopiaSqlColumn Boolean readFlag;              // 阅读状态0:未阅 1:已阅

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AgentNotifyUser.class, "userId", userId);
    }

    public static String ck_userId_notifyId(Long userId, Long notifyId) {
        return CacheKeyGenerator.generateCacheKey(AgentNotifyUser.class,
                new String[]{"userId", "notifyId"},
                new Object[]{userId, notifyId});
    }

    public static String ck_notifyId(Long notifyId) {
        return CacheKeyGenerator.generateCacheKey(AgentNotifyUser.class, "notifyId", notifyId);
    }
}
