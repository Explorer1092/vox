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

package com.voxlearning.utopia.service.business.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.NoCacheAsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.business.api.entity.ActivityData;

import javax.inject.Named;

/**
 * FIXME: 数据量很小，都加载到缓存中
 * FIXME: 这玩意还有用吗？不要缓存了
 * Created by fanshuo on 2015/5/26.
 */
@Named("com.voxlearning.utopia.service.business.impl.persistence.ActivityDataPersistence")
public class ActivityDataPersistence extends NoCacheAsyncStaticMongoPersistence<ActivityData, String> {

    public void deleteByActivityId(Long activityId) {
        Criteria criteria = Criteria.where("activityId").is(Long.toString(activityId));
        $executeRemove(createMongoConnection(), Query.query(criteria)).awaitUninterruptibly();
    }
}