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

package com.voxlearning.utopia.service.afenti.impl.persistence;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.afenti.api.entity.UserAfentiStats;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author tanguohong
 * @since 14-3-12
 */
@Named("com.voxlearning.utopia.service.afenti.impl.persistence.UserAfentiStatsPersistence")
@CacheBean(type = UserAfentiStats.class)
public class UserAfentiStatsPersistence extends AsyncStaticMongoPersistence<UserAfentiStats, Long> {

    @Override
    protected void calculateCacheDimensions(UserAfentiStats source, Collection<String> dimensions) {
        dimensions.add(UserAfentiStats.ck_id(source.getId()));
    }

    public void updateStats(Long id, String key, String pushExamDay) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        if (StringUtils.isBlank(pushExamDay)) {
            update = update.unset("stats." + key);
        } else {
            update = update.set("stats." + key, pushExamDay);
        }
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER);
        UserAfentiStats modified = $executeFindOneAndUpdate(createMongoConnection(), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(UserAfentiStats.ck_id(id))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
    }
}
