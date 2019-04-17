package com.voxlearning.utopia.service.zone.impl.persistence.giving;

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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.zone.api.entity.giving.ChickenStudentRecord;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author xuedongfeng
 * @date 2018-11-16
 */
@Repository
@CacheBean(type = ChickenStudentRecord.class, cacheName = "columb-zone-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class ChickenStudentRecordPersistence extends DynamicMongoShardPersistence<ChickenStudentRecord, String> {
    private static final String ID_SEP = "_";

    protected List<IMongoConnection> calculateMongoConnection(Integer activityId) {
        String mockId = activityId + "_9999l_00000l_0_1";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

    @CacheMethod
    public List<ChickenStudentRecord> findByClazzId(@CacheParameter("activityId") Integer activityId,@CacheParameter("schoolId") Long schoolId, @CacheParameter("clazzId") Long clazzId) {
        StringBuilder sbl = new StringBuilder();
        sbl.append("^").append(activityId).append(ID_SEP).append(schoolId).append(ID_SEP).append(clazzId).append(ID_SEP);
        Pattern pattern = Pattern.compile(sbl.toString());
        Criteria criteria = Criteria.where("_id").regex(pattern);
        Sort sort = new Sort(Sort.Direction.DESC, "ct");
        return $executeQuery(calculateMongoConnection(activityId), Query.query(criteria).with(sort)).getUninterruptibly();
    }

  @Override
  protected void calculateCacheDimensions(ChickenStudentRecord document, Collection<String> dimensions) {
      dimensions.add(cacheKeyFromId(document.getId()));
      dimensions.add(ChickenStudentRecord.cacheKeyFromActivityId(document.getActivityId()));
      dimensions.add(ChickenStudentRecord.cacheKeyFromClazzIdAndSchool(document.getActivityId(),document.getSchoolId(),document.getClazzId()));
  }

    @Override
    protected String calculateDatabase(String template, ChickenStudentRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, ChickenStudentRecord document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = document.getId().split("_");
        return StringUtils.formatMessage(template, SafeConverter.toInt(ids[0]));
    }
}
