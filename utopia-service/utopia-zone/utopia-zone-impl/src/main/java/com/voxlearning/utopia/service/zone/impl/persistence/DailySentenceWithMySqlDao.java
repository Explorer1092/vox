package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.zone.api.entity.WeekDailySentenceWithMySQL;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @Author yulong.ma
 * @Date 2018/10/23 1534
 * @Version1.0
 **/
@Repository
@CacheBean(type = WeekDailySentenceWithMySQL.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class DailySentenceWithMySqlDao extends
    StaticMySQLPersistence<WeekDailySentenceWithMySQL, Long> {

  @Override
  protected void calculateCacheDimensions(WeekDailySentenceWithMySQL document,
      Collection <String> dimensions) {
    dimensions.add(WeekDailySentenceWithMySQL.ck_rankListToClient());
  }

  @CacheMethod(type =WeekDailySentenceWithMySQL.class, key ="WeekDailySentence")
  public List<WeekDailySentenceWithMySQL> queryAll(){
    return query();
  }
}
