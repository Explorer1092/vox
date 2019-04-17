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
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotReceiveOtherRecord;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotStudentRecord;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author xuedongfeng
 *  2018-11
 */
@Repository
@CacheBean(type = PlotReceiveOtherRecord.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class PlotReceiveOtherRecordPersistence extends DynamicMongoShardPersistence<PlotReceiveOtherRecord, String> {

    protected List<IMongoConnection> calculateMongoConnection(Integer activityId) {
        String mockId = activityId + "_000";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

    @CacheMethod
    public List<PlotReceiveOtherRecord> findByUserId(@CacheParameter("activityId") Integer activityId, @CacheParameter("userId") Long userId) {
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "ct");
        return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria).with(sort)).getUninterruptibly();
    }

    @CacheMethod
    public List<PlotReceiveOtherRecord> findByUserIdAndOrder(@CacheParameter("activityId") Integer activityId, @CacheParameter("userId") Long userId ,@CacheParameter("orderId") String orderId) {
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId).and("orderId").is(orderId);
        Sort sort = new Sort(Sort.Direction.DESC, "ct");
        return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria).with(sort)).getUninterruptibly();
    }

    @Override
    protected void calculateCacheDimensions(PlotReceiveOtherRecord document, Collection<String> dimensions) {
        dimensions.add(PlotReceiveOtherRecord.cacheKeyFromUserId(document.getActivityId(), document.getUserId()));
        dimensions.add(PlotReceiveOtherRecord.cacheKeyFromUserIdAndOrderId(document.getActivityId(), document.getUserId(),document.getOrderId()));
    }

    @Override
    protected String calculateDatabase(String template, PlotReceiveOtherRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, PlotReceiveOtherRecord document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = document.getId().split("_");
        return StringUtils.formatMessage(template, SafeConverter.toInt(ids[0]));
    }
}
