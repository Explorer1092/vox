package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.dao.mongo.persistence.NoCacheAsyncStaticMongoPersistence;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.zone.api.entity.UserDailySentenceRecord;
import com.voxlearning.utopia.service.zone.api.entity.WeekDailySentenceWithMySQL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @Author yulong.ma
 * @Date 2018/10/23 1534
 * @Version1.0
 **/
@Repository
@CacheBean(type = UserDailySentenceRecord.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class UserDailySentenceDao extends
    StaticMongoShardPersistence <UserDailySentenceRecord, String> {

  @Override
  protected void calculateCacheDimensions(UserDailySentenceRecord document,
      Collection <String> dimensions) {
    dimensions.add(UserDailySentenceRecord.ck_id(document.getId()));
  }

  public UserDailySentenceRecord queryRecord(Integer year, Long userId) {
    return load(year + "_" + userId);
  }
}
