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
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotActivitySelectRecord;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author : kai.sun
 * @version : 2018-11-26
 * @description :
 **/
@Repository
@CacheBean(type = PlotActivitySelectRecord.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class PlotActivitySelectRecordPersistence extends DynamicMongoShardPersistence<PlotActivitySelectRecord,String> {

    private static final String ID_SEP = "_";

    protected List<IMongoConnection> calculateMongoConnection(Integer activityId,Long userId) {
        String mockId = activityId+ID_SEP+userId+ID_SEP;
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

    @Override
    protected String calculateDatabase(String template, PlotActivitySelectRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, PlotActivitySelectRecord document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = document.getId().split("_");
        return StringUtils.formatMessage(template, SafeConverter.toInt(ids[0]));
    }

    @Override
    protected void calculateCacheDimensions(PlotActivitySelectRecord document, Collection<String> dimensions) {
        dimensions.add(cacheKeyFromId(document.getId()));
        dimensions.add(PlotActivitySelectRecord.cacheKeyFromActivityIdUserId(document.getActivityId(),document.getUserId()));

    }

    @CacheMethod
    public List<PlotActivitySelectRecord> getPlotActivitySelectRecordList(@CacheParameter("activityId") Integer activityId,@CacheParameter("userId") Long userId){
        Pattern pattern = Pattern.compile("^" + activityId + ID_SEP + userId + ID_SEP);
        Criteria criteria = Criteria.where("_id").regex(pattern);
        return $executeQuery(calculateMongoConnection(activityId,userId), Query.query(criteria)).getUninterruptibly();
    }

}
