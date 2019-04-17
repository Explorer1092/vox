package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.zone.api.entity.DiscussZone;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author chensn
 * @date 2018-10-23 14:23
 */
@Repository
@CacheBean(type = DiscussZone.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class DiscussZonePersistence extends StaticMongoShardPersistence<DiscussZone, Integer> {

  @CacheMethod(key = "findUsedDiscuss")
  public List<DiscussZone> findUsedDiscuss() {
    Date now = new Date();
    Criteria criteria = Criteria.where("isShow").is(true).and("startDate").lte(now).and("endDate").gte(now);
    return query(new Query(criteria));
  }

  @Override
  protected void calculateCacheDimensions(DiscussZone document, Collection<String> dimensions) {
    dimensions.add(cacheKeyFromId(document.getId()));
    dimensions.add(document.ck_used_discuss());
  }
}
