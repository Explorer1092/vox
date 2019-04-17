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
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiGuide;
import com.voxlearning.utopia.service.afenti.api.entity.UserAfentiGuide;

import javax.inject.Named;
import java.util.Collection;

@Named("com.voxlearning.utopia.service.afenti.impl.persistence.UserAfentiGuidePersistence")
@CacheBean(type = UserAfentiGuide.class)
public class UserAfentiGuidePersistence extends AsyncStaticMongoPersistence<UserAfentiGuide, Long> {

    @Override
    protected void calculateCacheDimensions(UserAfentiGuide source, Collection<String> dimensions) {
        dimensions.add(UserAfentiGuide.ck_id(source.getId()));
    }

    @Override
    public UserAfentiGuide load(Long id) {
        if (id == null) return null;
        UserAfentiGuide neonatal = new UserAfentiGuide();
        neonatal.setId(id);
        neonatal.initialize();
        return insertIfAbsent(id, neonatal);
    }

    public void completeGuide(Long userId, AfentiGuide guide) {
        if (userId == null || guide == null) return;
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = Update.update("guides." + guide.name(), true);
        for (AfentiGuide each : AfentiGuide.values()) {
            if (each == guide) continue;
            update = update.setOnInsert("guides." + each.name(), false);
        }
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER);
        UserAfentiGuide modified = $executeFindOneAndUpdate(createMongoConnection(), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(UserAfentiGuide.ck_id(userId))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
    }
}
