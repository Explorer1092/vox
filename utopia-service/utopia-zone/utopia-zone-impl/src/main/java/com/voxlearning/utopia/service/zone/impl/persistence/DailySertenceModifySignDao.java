package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort.Direction;
import com.voxlearning.utopia.service.zone.api.entity.WeekDailySentence;
import com.voxlearning.utopia.service.zone.api.entity.plot.DailySertenceModifySignConfig;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @Author yulong.ma
 * @Date 2018/10/23 1534
 * @Version1.0
 **/
@Repository
@CacheBean(type = DailySertenceModifySignConfig.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class DailySertenceModifySignDao extends StaticMongoShardPersistence<DailySertenceModifySignConfig,Integer> {

  @Override
  protected void calculateCacheDimensions(DailySertenceModifySignConfig document,
      Collection<String> dimensions) {
    dimensions.add(DailySertenceModifySignConfig.ck_DailySertenceModifySignConfig());
  }

  @CacheMethod(type =DailySertenceModifySignConfig.class, key ="DailySertenceModifySignConfig")
  public List<DailySertenceModifySignConfig> queryAll(){
    Criteria criteria =  Criteria.where("isShow").is(true);
    Query query = new Query(criteria);
    return query(query);
  }

}
