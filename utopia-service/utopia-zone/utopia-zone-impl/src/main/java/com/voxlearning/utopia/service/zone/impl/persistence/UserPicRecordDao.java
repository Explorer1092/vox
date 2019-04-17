package com.voxlearning.utopia.service.zone.impl.persistence;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.connection.IMongoConnection;
import com.voxlearning.alps.dao.mongo.hash.MongoShardCalculator;
import com.voxlearning.alps.dao.mongo.hash.MongoShardCalculatorFactory;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort.Direction;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.zone.api.entity.UserPicRecord;
import com.voxlearning.utopia.service.zone.api.entity.WeekDailySentence;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * @Author yulong.ma
 * @Date 2018/10/23 1534
 * @Version1.0
 **/
@Repository
@CacheBean(type = UserPicRecord.class,cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class UserPicRecordDao extends DynamicMongoShardPersistence<UserPicRecord,String> {

  @Override
  protected void calculateCacheDimensions(UserPicRecord document, Collection <String> dimensions) {

  }

  @Override
  protected String calculateDatabase(String template, UserPicRecord document) {
    return null;
  }

  @Override
  protected String calculateCollection(String template, UserPicRecord document) {
    Objects.requireNonNull(document);
    Objects.requireNonNull(document.getId());
    String[] ids = document.getId().split("_");
    if (RuntimeMode.le(Mode.TEST)) {
      return StringUtils.formatMessage(template, SafeConverter.toLong(ids[0]) % 2);
    } else {
      return StringUtils.formatMessage(template, SafeConverter.toLong(ids[0]) % 100);
    }
  }
}
