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
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.zone.api.entity.ZoneClazzRewardNotice;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author chensn
 * @date 2018-11-23
 */
@Repository
@CacheBean(type = ZoneClazzRewardNotice.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.OTHER_FIELDS)
public class ZoneClazzRewardPersistence extends DynamicMongoShardPersistence<ZoneClazzRewardNotice, String> {
    private static final String ID_SEP = "_";

    protected List<IMongoConnection> calculateMongoConnection(Integer activityId) {
        String mockId = activityId + "_9999l_00000l";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }


    @Override
    protected String calculateDatabase(String template, ZoneClazzRewardNotice document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, ZoneClazzRewardNotice document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = document.getId().split(ID_SEP);
        return StringUtils.formatMessage(template, SafeConverter.toInt(ids[0]));
    }

    @Override
    protected void calculateCacheDimensions(ZoneClazzRewardNotice document, Collection<String> dimensions) {
        dimensions.add(ZoneClazzRewardNotice.cacheKeyFromActivityIdAndClazzId(document.getActivityId(),document.getClazzId()));
        dimensions.add(ZoneClazzRewardNotice.cacheKeyFromUserId(document.getActivityId(), document.getUserId()));
        dimensions.add(ZoneClazzRewardNotice.cacheKeyFromAll(document.getActivityId(), document.getUserId(), document.getRewardType()));
        dimensions.add(ZoneClazzRewardNotice.cacheKeyFromClazzAndType(document.getActivityId(), document.getClazzId(), document.getRewardType()));
    }


    @CacheMethod
    public List<ZoneClazzRewardNotice> findByClazzId(@CacheParameter("activityId") Integer activityId, @CacheParameter("clazzId") Long clazzId) {
        Criteria criteria = Criteria.where("clazzId").is(clazzId).and("activityId").is(activityId);
        Sort sort = new Sort(Sort.Direction.DESC, "ct");
        List<ZoneClazzRewardNotice> result = $executeQuery(calculateMongoConnection(activityId), Query.query(criteria).with(sort)).getUninterruptibly();
        return result;
    }

    @CacheMethod
    public List<ZoneClazzRewardNotice> findByUserId(@CacheParameter("activityId") Integer activityId, @CacheParameter("userId") Long userId) {
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "ct");
        List<ZoneClazzRewardNotice> result = $executeQuery(calculateMongoConnection(activityId), Query.query(criteria).with(sort)).getUninterruptibly();
        return result;
    }

    @CacheMethod
    public List<ZoneClazzRewardNotice> findByUserIdAndRewardType(@CacheParameter("activityId") Integer activityId, @CacheParameter("userId") Long userId, @CacheParameter("rewardType") Integer rewardType) {
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId).and("rewardType").is(rewardType);
        Sort sort = new Sort(Sort.Direction.DESC, "ct");
        List<ZoneClazzRewardNotice> result = $executeQuery(calculateMongoConnection(activityId), Query.query(criteria).with(sort)).getUninterruptibly();
        return result;
    }

    @CacheMethod
    public List<ZoneClazzRewardNotice> findByClazzIdAndReward(@CacheParameter("activityId") Integer activityId, @CacheParameter("clazzId") Long clazzId, @CacheParameter("rewardType") Integer rewardType) {
        Criteria criteria = Criteria.where("clazzId").is(clazzId).and("activityId").is(activityId).and("rewardType").is(rewardType);
        Sort sort = new Sort(Sort.Direction.DESC, "ct");
        List<ZoneClazzRewardNotice> result = $executeQuery(calculateMongoConnection(activityId), Query.query(criteria).with(sort)).getUninterruptibly();
        return result;
    }
}
