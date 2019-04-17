package com.voxlearning.utopia.service.zone.impl.persistence.giving;

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
import com.voxlearning.utopia.service.zone.api.entity.giving.SelfActivityHelp;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author : kai.sun
 * @version : 2018-11-15
 * @description :
 **/

@Repository
@CacheBean(type = SelfActivityHelp.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class SelfActivityHelpPersistence extends DynamicMongoShardPersistence<SelfActivityHelp,String> {

    private static final String ID_SEP = "_";

    protected List<IMongoConnection> calculateMongoConnection(Integer activityId) {
        String mockId = activityId + "_9999l_00000l_0_1_1";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

    @CacheMethod
    public List<SelfActivityHelp> findByClazzId(@CacheParameter("activityId") Integer activityId, @CacheParameter("schoolId") Long schoolId, @CacheParameter("clazzId") Long clazzId) {
        Pattern pattern = Pattern.compile("^" + activityId + ID_SEP + schoolId + ID_SEP + clazzId + ID_SEP);
        Criteria criteria = Criteria.where("_id").regex(pattern);
        return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria)).getUninterruptibly();
    }

    @CacheMethod
    public List<SelfActivityHelp> findByUserId(@CacheParameter("activityId") Integer activityId,@CacheParameter("schoolId") Long schoolId,@CacheParameter("clazzId") Long clazzId, @CacheParameter("userId") Long userId) {
        Pattern pattern = Pattern.compile("^" + activityId + ID_SEP + schoolId + ID_SEP + clazzId + ID_SEP + userId + ID_SEP);
        Criteria criteria = Criteria.where("_id").regex(pattern);
        return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria)).getUninterruptibly();
    }

    @Override
    protected void calculateCacheDimensions(SelfActivityHelp document, Collection<String> dimensions) {
        dimensions.add(cacheKeyFromId(document.getId()));
        dimensions.add(SelfActivityHelp.cacheKeyFromActivityId(document.getActivityId()));
        dimensions.add(SelfActivityHelp.cacheKeyFromClazzIdAndSchoolAndUserId(document.getActivityId(),document.getSchoolId(),document.getClazzId(),document.getUserId()));
        dimensions.add(SelfActivityHelp.cacheKeyFromClazzIdAndSchool(document.getActivityId(),document.getSchoolId(),document.getClazzId()));
    }

    @Override
    protected String calculateDatabase(String template, SelfActivityHelp document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, SelfActivityHelp document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = document.getId().split("_");
        return StringUtils.formatMessage(template, SafeConverter.toInt(ids[0]));
    }

}
