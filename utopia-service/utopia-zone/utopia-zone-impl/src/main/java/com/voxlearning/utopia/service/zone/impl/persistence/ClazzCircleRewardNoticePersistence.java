package com.voxlearning.utopia.service.zone.impl.persistence;

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
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import com.voxlearning.utopia.service.zone.api.entity.ClazzCircleRewardNotice;
import com.voxlearning.utopia.service.zone.api.entity.giving.ChickenHelp;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author chensn
 * @date 2018-10-30
 */
@Repository
@CacheBean(type = ClazzCircleRewardNotice.class, cacheName = "columb-zone-cache")
public class ClazzCircleRewardNoticePersistence extends DynamicMongoShardPersistence<ClazzCircleRewardNotice, String> {
    private static final String ID_SEP = "_";

  protected List<IMongoConnection> calculateMongoConnection(Integer activityId) {
    String mockId = activityId + "_9999l_00000l_0";
    MongoNamespace namespace = calculateIdMongoNamespace(mockId);
    return createMongoConnection(namespace);
  }

  @CacheMethod
  public List<ClazzCircleRewardNotice> findByClazzId(@CacheParameter("activityId") Integer activityId,@CacheParameter("schoolId") Long schoolId, @CacheParameter("clazzId") Long clazzId) {
    StringBuilder sbl = new StringBuilder();
    sbl.append("^").append(activityId).append(ID_SEP).append(schoolId).append(ID_SEP).append(clazzId).append(ID_SEP);
    Pattern pattern = Pattern.compile(sbl.toString());
    Criteria criteria = Criteria.where("_id").regex(pattern);
    return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria)).getUninterruptibly();
  }

  @CacheMethod
  public List<ClazzCircleRewardNotice> findByUserId(@CacheParameter("activityId") Integer activityId,@CacheParameter("userId") Long userId) {
    StringBuilder sbl = new StringBuilder();
    sbl.append("^").append(activityId).append(ID_SEP).append(userId).append(ID_SEP);
    Pattern pattern = Pattern.compile(sbl.toString());
    Criteria criteria = Criteria.where("_id").regex(pattern);
    return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria)).getUninterruptibly();
  }

  @CacheMethod
  public List<ClazzCircleRewardNotice> findByActivityId(@CacheParameter("activityId") Integer activityId) {
    StringBuilder sbl = new StringBuilder();
    sbl.append("^").append(activityId).append(ID_SEP);
    Pattern pattern = Pattern.compile(sbl.toString());
    Criteria criteria = Criteria.where("_id").regex(pattern);
    return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria)).getUninterruptibly();
  }

  @Override
  protected void calculateCacheDimensions(ClazzCircleRewardNotice document, Collection<String> dimensions) {
    dimensions.add(cacheKeyFromId(document.getId()));
    dimensions.add(ClazzCircleRewardNotice.cacheKeyFromActivityId(document.getActivityId()));
    dimensions.add(ClazzCircleRewardNotice.cacheKeyFromClazzIdAndUserId(document.getActivityId(),document.getUserId()));
  }

  @Override
  protected String calculateDatabase(String template, ClazzCircleRewardNotice document) {
    return null;
  }

  @Override
  protected String calculateCollection(String template, ClazzCircleRewardNotice document) {
    Objects.requireNonNull(document);
    Objects.requireNonNull(document.getId());
    String[] ids = document.getId().split("_");
    return StringUtils.formatMessage(template, SafeConverter.toInt(ids[0]));
  }
}
