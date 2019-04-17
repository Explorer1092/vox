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

package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentNotifyUser;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Notify User Persistence
 * Created by Shuai.Huan on 2014/7/21.
 */
@Named
@UtopiaCacheSupport(AgentNotifyUser.class)
public class AgentNotifyUserPersistence extends StaticPersistence<Long, AgentNotifyUser> {

    @Override
    protected void calculateCacheDimensions(AgentNotifyUser source, Collection<String> dimensions) {
        dimensions.add(AgentNotifyUser.ck_userId(source.getUserId()));
        dimensions.add(AgentNotifyUser.ck_userId_notifyId(source.getUserId(), source.getNotifyId()));
        dimensions.add(AgentNotifyUser.ck_notifyId(source.getNotifyId()));
    }

    @UtopiaCacheable
    public List<AgentNotifyUser> findByUserId(@UtopiaCacheKey(name = "userId") Long userId) {
        return withSelectFromTable("WHERE USER_ID=? AND CREATE_DATETIME >= DATE_ADD(NOW(), INTERVAL -3 MONTH) ORDER BY CREATE_DATETIME DESC").useParamsArgs(userId).queryAll();
    }

    @UtopiaCacheable
    public AgentNotifyUser findByUserIdAndNotifyId(@UtopiaCacheKey(name = "userId") Long userId,
                                                   @UtopiaCacheKey(name = "notifyId") Long notifyId) {
        return withSelectFromTable("WHERE USER_ID=? AND NOTIFY_ID=? AND READ_FLAG=FALSE").useParamsArgs(userId, notifyId).queryObject();
    }

    @UtopiaCacheable
    public List<AgentNotifyUser> findByNotifyId(@UtopiaCacheKey(name = "notifyId") Long notifyId) {
        return withSelectFromTable("WHERE NOTIFY_ID=?").useParamsArgs(notifyId).queryAll();
    }
}
