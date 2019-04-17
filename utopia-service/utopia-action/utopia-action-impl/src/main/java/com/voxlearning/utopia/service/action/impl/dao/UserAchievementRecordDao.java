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

import com.mongodb.MongoCommandException;
import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.action.api.document.UserAchievementRecord;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * DAO of {@link UserAchievementRecord}.
 *
 * @author Xiaohai Zhang
 * @since Aug 4, 2016
 */
@Named("com.voxlearning.utopia.service.action.impl.dao.UserAchievementRecordDao")
@CacheBean(type = UserAchievementRecord.class, useValueWrapper = true)
public class UserAchievementRecordDao extends DynamicCacheDimensionDocumentMongoDao<UserAchievementRecord, String> {

    @Override
    protected String calculateDatabase(String template, UserAchievementRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, UserAchievementRecord document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        UserAchievementRecord.UserAchievementRecordId docId = document.parse();
        long mod = docId.getUserId() % 100;
        return StringUtils.formatMessage(template, mod);
    }

    @CacheMethod
    public List<UserAchievementRecord> findByUser(@CacheParameter("UID") Long userId) {
        if (userId == null || userId <= 0L) {
            return Collections.emptyList();
        }

        Pattern pattern = Pattern.compile("^" + userId + UserAchievementRecord.ID_SEP);
        Criteria criteria = Criteria.where("_id").regex(pattern);
        Query query = Query.query(criteria);

        String mockId = new UserAchievementRecord.UserAchievementRecordId(userId, "000").toString();
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        MongoConnection connection = createMongoConnection(namespace);

        return executeQuery(connection, query);
    }

    public UserAchievementRecord upsertAchievement(Long userId, ActionEventType type, int delta) {
        String realId = new UserAchievementRecord.UserAchievementRecordId(userId, type.name()).toString();

        Date current = new Date();
        Criteria criteria = Criteria.where("_id").is(realId);
        Update update = new Update()
                .inc("score", delta)
                .set("ut", current)
                .setOnInsert("ct", current);
        BsonDocument f = criteriaTranslator.translate(criteria);
        BsonDocument u = updateTranslator.translate(update);
        MongoNamespace namespace = calculateIdMongoNamespace(realId);
        MongoConnection connection = createMongoConnection(namespace);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER);
        BsonDocument ret;

        try {
            ret = connection.collection.findOneAndUpdate(f, u, options);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {// duplicate key case
                ret = connection.collection.findOneAndUpdate(f, u, options);
            } else {
                logger.error("error occurred when upsert user achievement: id={}, type={}", userId, type, ex);
                throw ex;
            }
        }

        UserAchievementRecord modified = convertBsonDocument(ret);
        if (modified != null) {
            // cache key1 : by id
            String ck1 = UserAchievementRecord.ck_id(modified.getId());
            getCache().set(ck1, getDefaultCacheExpirationInSeconds(), modified);

            // cache key2 : by userId
            String ck2 = UserAchievementRecord.ck_uid(modified.parse().getUserId());
            if (getCache().load(ck2) != null) {
                ChangeCacheObject<List<UserAchievementRecord>> modifier = userAchievements -> {
                    UserAchievementRecord exists = userAchievements.stream().filter(e -> Objects.equals(e.getId(), modified.getId())).findAny().orElse(null);
                    if (exists == null) {
                        userAchievements.add(0, modified);
                    } else {
                        userAchievements.remove(exists);
                        userAchievements.add(modified);
                    }
                    return userAchievements;
                };
                CacheValueModifierExecutor<List<UserAchievementRecord>> executor = getCache().createCacheValueModifier();
                executor.key(ck2)
                        .expiration(getDefaultCacheExpirationInSeconds())
                        .modifier(modifier)
                        .execute();
            }
        }
        return modified;
    }

}
