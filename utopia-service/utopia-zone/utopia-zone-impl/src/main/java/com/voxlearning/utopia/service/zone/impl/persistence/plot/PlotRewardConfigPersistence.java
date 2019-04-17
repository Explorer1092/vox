package com.voxlearning.utopia.service.zone.impl.persistence.plot;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotRewardConfig;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author xuedongfeng
 * @date 2018-11
 */
@Repository
@CacheBean(type = PlotRewardConfig.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class PlotRewardConfigPersistence extends StaticMongoShardPersistence<PlotRewardConfig, String> {

    @CacheMethod
    public List<PlotRewardConfig> findByActivityId(@CacheParameter("activityId") Integer activityId) {
        Pattern pattern = Pattern.compile("^" + activityId + "_");
        Criteria criteria = Criteria.where("_id").regex(pattern);
        Sort sort = new Sort(Sort.Direction.ASC, "type");
        return query(new Query(criteria).with(sort));
    }

    @Override
    protected void calculateCacheDimensions(PlotRewardConfig document, Collection<String> dimensions) {
      dimensions.add(cacheKeyFromId(document.getId()));
      dimensions.add(PlotRewardConfig.cacheKeyFromActivityId(document.getActivityId()));
    }
}
