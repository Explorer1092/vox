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
@CacheBean(type = ClazzActivityRecord.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class ClazzActivityRecordPersistence extends DynamicMongoShardPersistence<ClazzActivityRecord, String> {
    private static final String ID_SEP = "_";

    protected List<IMongoConnection> calculateMongoConnection(Integer activityId) {
        String mockId = activityId + "_9999l_00000l";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

    @CacheMethod
    public List<ClazzActivityRecord> findBySchooldId(@CacheParameter("activityId") Integer activityId, @CacheParameter("schoolId") Long schoolId) {
        StringBuilder sbl = new StringBuilder();
        sbl.append("^").append(activityId).append(ID_SEP).append(schoolId).append(ID_SEP);
        Pattern pattern = Pattern.compile(sbl.toString());
        Criteria criteria = Criteria.where("_id").regex(pattern);
        List<ClazzActivityRecord> result = $executeQuery(calculateMongoConnection(activityId), Query.query(criteria)).getUninterruptibly();
        return result;
    }

    @CacheMethod
    public List<ClazzActivityRecord> findByClazzId(@CacheParameter("activityId") Integer activityId, @CacheParameter("schoolId") Long schoolId, @CacheParameter("clazzId") Long clazzId) {
        StringBuilder sbl = new StringBuilder();
        sbl.append("^").append(activityId).append(ID_SEP).append(schoolId).append(ID_SEP).append(clazzId).append(ID_SEP);
        Pattern pattern = Pattern.compile(sbl.toString());
        Criteria criteria = Criteria.where("_id").regex(pattern);
        List<ClazzActivityRecord> result = $executeQuery(calculateMongoConnection(activityId), Query.query(criteria)).getUninterruptibly();
        return result;
    }

  @Override
  protected void calculateCacheDimensions(ClazzActivityRecord document, Collection<String> dimensions) {
      dimensions.add(cacheKeyFromId(document.getId()));
      dimensions.add(ClazzActivityRecord.cacheKeyFromActivityId(document.getActivityId()));
      dimensions.add(ClazzActivityRecord.cacheKeyFromClazzId(document.getActivityId(), document.getSchoolId(), document.getClazzId()));
      dimensions.add(ClazzActivityRecord.cacheKeyFromSchoolId(document.getActivityId(), document.getSchoolId()));
  }

    public void updateOrSave(ClazzActivityRecord clazzActivityRecord){
        upsert(clazzActivityRecord);
    }

    @Override
    protected String calculateDatabase(String template, ClazzActivityRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, ClazzActivityRecord document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = document.getId().split("_");
        return StringUtils.formatMessage(template, SafeConverter.toInt(ids[0]));
    }
}
