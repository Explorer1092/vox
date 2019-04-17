package com.voxlearning.utopia.service.zone.impl.persistence;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.connection.IMongoConnection;
import com.voxlearning.alps.dao.mongo.hash.MongoShardCalculator;
import com.voxlearning.alps.dao.mongo.hash.MongoShardCalculatorFactory;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRankLikeRecord;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author chensn
 * @date 2018-12-10
 */
@Repository
@CacheBean(type = ClazzActivityRankLikeRecord.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class ClazzActivityRankLikePersistence extends DynamicMongoShardPersistence<ClazzActivityRankLikeRecord, String> {
    private static final String ID_SEP = "_";

    @Override
    protected void calculateCacheDimensions(ClazzActivityRankLikeRecord document, Collection<String> dimensions) {
        dimensions.add(cacheKeyFromId(document.getId()));
        dimensions.add(document.ck_userId_actityId(document.getUserId(), document.getActivityId()));
    }

    @Override
    protected String calculateDatabase(String template, ClazzActivityRankLikeRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, ClazzActivityRankLikeRecord document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = document.getId().split("_");
        return StringUtils.formatMessage(template, SafeConverter.toInt(ids[1]));
    }

    @Override
    protected MongoShardCalculator getShardCalculator(int shardSize) {
        return new MongoShardCalculator() {
            @Override
            public int calculate(Object id) {
                String[] idParts = StringUtils.split(SafeConverter.toString(id), "_");
                if (idParts.length == 0) {
                    return MongoShardCalculatorFactory.getInstance().getCalculator(shardSize).calculate(id);
                } else {
                    return MongoShardCalculatorFactory.getInstance().getCalculator(shardSize).calculate(idParts[0]);
                }
            }

            @Override
            public void close() {

            }
        };
    }

    @CacheMethod
    public List<ClazzActivityRankLikeRecord> getUserLikeRecord(@CacheParameter("userId") Long userId, @CacheParameter("activityId") Integer activityId) {
        StringBuilder sbl = new StringBuilder();
        sbl.append("^").append(userId).append(ID_SEP).append(activityId).append(ID_SEP);
        Pattern pattern = Pattern.compile(sbl.toString());
        Criteria criteria = Criteria.where("_id").regex(pattern);
        MongoNamespace namespace = calculateIdMongoNamespace(userId + "_" + activityId);
        IMongoConnection mongoConnection = createMongoConnection(namespace, userId + "_" + activityId);
        return $executeQuery(Collections.singleton(mongoConnection), Query.query(criteria)).getUninterruptibly();
    }
}
