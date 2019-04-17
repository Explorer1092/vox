package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivity;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author chensn
 * @date 2018-10-30
 */
@Repository
@CacheBean(type = ClazzActivity.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class ClazzActivityPersistence extends StaticMongoShardPersistence<ClazzActivity, Integer> {

  @CacheMethod(key = "findUsedActivity")
  public List<ClazzActivity> findUsedActivity() {
    Date now = new Date();
    long timestamp = now.getTime() - (43200L * 1000L);
    Date date = new Date(timestamp);
    Criteria criteria = Criteria.where("isShow").is(true).and("startDate").lte(now).and("endDate").gte(date);
    return query(new Query(criteria).with(new Sort(Sort.Direction.DESC, "sort")));
  }

  @Override
  protected void calculateCacheDimensions(ClazzActivity document, Collection<String> dimensions) {
    dimensions.add(cacheKeyFromId(document.getId()));
    dimensions.add(document.ck_last_activity());
    dimensions.add(document.ck_uesd_activity());
  }

  @CacheMethod(key = "findTheLastActivity")
  public ClazzActivity findTheLastActivity() {
    Date now = new Date();
    Criteria criteria = Criteria.where("endDate").lte(now);
    List<ClazzActivity> clazzActivity = query(new Query(criteria).with(new Sort(Sort.Direction.DESC, "endDate")).limit(1));
    return CollectionUtils.isNotEmpty(clazzActivity) ? clazzActivity.get(0) : null;
  }

}
