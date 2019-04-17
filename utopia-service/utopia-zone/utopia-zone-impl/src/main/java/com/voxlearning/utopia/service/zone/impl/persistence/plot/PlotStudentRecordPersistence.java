package com.voxlearning.utopia.service.zone.impl.persistence.plot;

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
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotStudentRecord;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author xuedongfeng
 * @date 2018-11-16
 */
@Repository
@CacheBean(type = PlotStudentRecord.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class PlotStudentRecordPersistence extends DynamicMongoShardPersistence<PlotStudentRecord, String> {

    protected List<IMongoConnection> calculateMongoConnection(Integer activityId) {
        String mockId = activityId + "_000";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

    @CacheMethod
    public List<PlotStudentRecord> findByUserId(@CacheParameter("activityId") Integer activityId, @CacheParameter("userId") Long userId) {
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "ct");
        return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria).with(sort)).getUninterruptibly();
    }

    @Override
    protected void calculateCacheDimensions(PlotStudentRecord document, Collection<String> dimensions) {
        dimensions.add(PlotStudentRecord.cacheKeyFromUserId(document.getActivityId(), document.getUserId()));
    }

    @Override
    protected String calculateDatabase(String template, PlotStudentRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, PlotStudentRecord document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = document.getId().split("_");
        return StringUtils.formatMessage(template, SafeConverter.toInt(ids[0]));
    }
}
