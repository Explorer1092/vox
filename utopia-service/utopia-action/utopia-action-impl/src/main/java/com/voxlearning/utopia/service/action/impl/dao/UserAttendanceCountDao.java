package com.voxlearning.utopia.service.action.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.utopia.service.action.api.document.UserAttendanceCount;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.Date;
import java.util.Objects;

/**
 * @author xinxin
 * @since 11/10/2016
 */
@Named("com.voxlearning.utopia.service.action.impl.dao.UserAttendanceCountDao")
@CacheBean(type = UserAttendanceCount.class, useValueWrapper = true)
@CacheDimension(value = CacheDimensionDistribution.ID_FIELD)
public class UserAttendanceCountDao extends DynamicCacheDimensionDocumentMongoDao<UserAttendanceCount, String> {
    @Override
    protected String calculateDatabase(String template, UserAttendanceCount document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());

        String[] ids = document.getId().split("-");
        if (ids.length != 2) throw new IllegalArgumentException();
        if (ids[1].length() != 6) throw new IllegalArgumentException();

        return StringUtils.formatMessage(template, ids[1]);
    }

    @Override
    protected String calculateCollection(String template, UserAttendanceCount document) {
        return null;
    }

    public UserAttendanceCount incrUserAttendanceCount(Long userId) {
        String id = UserAttendanceCount.generateId(userId);

        Criteria criteria = Criteria.where("_id").is(id);

        Update update = new Update().setOnInsert("_id", id)
                .setOnInsert("userId", userId)
                .setOnInsert("ct", new Date())
                .inc("count", 1)
                .currentDate("ut");

        BsonDocument f = criteriaTranslator.translate(criteria);
        BsonDocument u = updateTranslator.translate(update);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER);

        BsonDocument modified = calculateMongoConnection(userId).collection.findOneAndUpdate(f, u, options);
        UserAttendanceCount userAttendanceCount = convertBsonDocument(modified);

        getCache().createCacheValueModifier()
                .key(CacheKeyGenerator.generateCacheKey(UserAttendanceCount.class, id))
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> userAttendanceCount)
                .execute();

        return userAttendanceCount;
    }

    private MongoConnection calculateMongoConnection(Long userId) {
        String mockId = UserAttendanceCount.generateId(userId);
        MongoNamespace mongoNamespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(mongoNamespace);
    }

}
