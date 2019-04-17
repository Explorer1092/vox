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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.zone.api.entity.giving.ChickenClazzProgress;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author xuedongfeng
 * @date 2018-11-16
 */
@Repository
@CacheBean(type = ChickenClazzProgress.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class ChickenClazzProgressPersistence extends DynamicMongoShardPersistence<ChickenClazzProgress, String> {
    private static final String ID_SEP = "_";

    protected List<IMongoConnection> calculateMongoConnection(Integer activityId) {
        String mockId = activityId + "_9999l_00000l";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

    @CacheMethod
    public List<ChickenClazzProgress> findByClazzId(@CacheParameter("activityId") Integer activityId, @CacheParameter("schoolId") Long schoolId, @CacheParameter("clazzId") Long clazzId) {
        StringBuilder sbl = new StringBuilder();
        sbl.append("^").append(activityId).append(ID_SEP).append(schoolId).append(ID_SEP).append(clazzId).append(ID_SEP);
        Pattern pattern = Pattern.compile(sbl.toString());
        Criteria criteria = Criteria.where("_id").regex(pattern);
        Sort sort = new Sort(Sort.Direction.DESC, "count");
        return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria).with(sort)).getUninterruptibly();
    }

    public List<ChickenClazzProgress> findByActivityId(Integer activityId, Integer page, Integer pageSize) {
        int realPage = page <= 0 ? 0 : page;
        Pattern pattern = Pattern.compile("^" + activityId + "_");
        Criteria criteria = Criteria.where("_id").regex(pattern);
        return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria).skip(realPage * pageSize).limit(pageSize)).getUninterruptibly();
    }

    public Long findCountByActivityId(Integer activityId) {
        Pattern pattern = Pattern.compile("^" + activityId + "_");
        Criteria criteria = Criteria.where("_id").regex(pattern);
        return $executeCount(calculateMongoConnection(activityId), Query.query(criteria)).getUninterruptibly();
    }

    @Override
    protected void calculateCacheDimensions(ChickenClazzProgress document, Collection<String> dimensions) {
        dimensions.add(cacheKeyFromId(document.getId()));
        dimensions.add(ChickenClazzProgress.cacheKeyFromClazzId(document.getActivityId(), document.getSchoolId(), document.getClazzId()));
    }

    @Override
    protected String calculateDatabase(String template, ChickenClazzProgress document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, ChickenClazzProgress document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = document.getId().split("_");
        return StringUtils.formatMessage(template, SafeConverter.toInt(ids[0]));
    }
}
