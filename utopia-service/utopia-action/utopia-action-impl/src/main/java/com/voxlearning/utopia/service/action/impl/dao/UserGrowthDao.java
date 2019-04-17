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

package com.voxlearning.utopia.service.action.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.utopia.service.action.api.document.UserGrowth;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.Date;
import java.util.Objects;

@Named("com.voxlearning.utopia.service.action.impl.dao.UserGrowthDao")
@CacheBean(type = UserGrowth.class, useValueWrapper = true)
public class UserGrowthDao extends DynamicCacheDimensionDocumentMongoDao<UserGrowth, Long> {

    @Override
    protected String calculateDatabase(String template, UserGrowth document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, UserGrowth document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        long mod = document.getId() % 100;
        return StringUtils.formatMessage(template, mod);
    }

    public UserGrowth addAndGet(Long id, int delta) {
        Date current = new Date();
        boolean upsert = true;
        Criteria criteria;
        if (delta >= 0) {
            criteria = Criteria.where("_id").is(id);
        } else {
            upsert = false;
            criteria = Criteria.where("_id").is(id).and("gv").gte(-delta);
        }
        Update update = new Update()
                .inc("gv", delta)
                .set("ut", current)
                .setOnInsert("ct", current);
        BsonDocument f = criteriaTranslator.translate(criteria);
        BsonDocument u = updateTranslator.translate(update);
        MongoNamespace namespace = calculateIdMongoNamespace(id);
        MongoConnection connection = createMongoConnection(namespace);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(upsert)
                .returnDocument(ReturnDocument.AFTER);
        UserGrowth modified = convertBsonDocument(connection.collection.findOneAndUpdate(f, u, options));
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(CacheKeyGenerator.generateCacheKey(UserGrowth.class, id))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
        return modified;
    }
}
