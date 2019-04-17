package com.voxlearning.utopia.service.action.impl.dao;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.document.ClazzAttendanceCount;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author xinxin
 * @since 19/8/2016
 */
@Named("com.voxlearning.utopia.service.action.impl.dao.ClazzAttendanceCountDao")
@CacheBean(type = ClazzAttendanceCount.class)
public class ClazzAttendanceCountDao extends DynamicCacheDimensionDocumentMongoDao<ClazzAttendanceCount, String> {
    @Override
    protected String calculateDatabase(String template, ClazzAttendanceCount document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());

        String[] ids = document.getId().split("-");
        if (ids.length != 3) throw new IllegalArgumentException();
        if (ids[1].length() != 8) throw new IllegalArgumentException();

        return StringUtils.formatMessage(template, ids[1].substring(0, 6));
    }

    @Override
    protected String calculateCollection(String template, ClazzAttendanceCount document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());

        String[] ids = document.getId().split("-");
        if (ids.length != 3) throw new IllegalArgumentException();
        Long schoolId = SafeConverter.toLong(ids[0], -1);
        if (-1 == schoolId) throw new IllegalArgumentException();

        return StringUtils.formatMessage(template, schoolId % 100);
    }

    //数据量太大，业务接口上做缓存，这里直接查
    public List<ClazzAttendanceCount> findBySchoolId(Long schoolId) {
        String day = getDay();
        Pattern pattern = Pattern.compile("^" + schoolId + "-" + day + "-");

        Criteria criteria = Criteria.where("_id").regex(pattern);

        return executeQuery(calculateMongoConnection(schoolId), Query.query(criteria));
    }

    public ClazzAttendanceCount incrAttendanceCount(Long schoolId, Long clazzId, Integer totalStudentCount) {
        String id = ClazzAttendanceCount.generateId(schoolId, clazzId);

        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().setOnInsert("_id", id)
                .setOnInsert("clazzId", clazzId)
                .setOnInsert("schoolId", schoolId)
                .setOnInsert("totalCount", totalStudentCount)
                .setOnInsert("day", getDay())
                .setOnInsert("ct", new Date())
                .inc("count", 1)
                .currentDate("ut");

        BsonDocument f = criteriaTranslator.translate(criteria);
        BsonDocument u = updateTranslator.translate(update);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER);

        BsonDocument modified;
        try {
            modified = calculateMongoConnection(schoolId).collection.findOneAndUpdate(f, u, options);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                modified = calculateMongoConnection(schoolId).collection.findOneAndUpdate(f, u, options);
            } else {
                logger.error("error occurred with regioncode:{},clazzId:{},count:{}", schoolId, clazzId, totalStudentCount, ex);
                throw ex;
            }
        }

        ClazzAttendanceCount clazzAttendanceCount = convertBsonDocument(modified);
        if (null != clazzAttendanceCount) {
            getCache().createCacheValueModifier()
                    .key(CacheKeyGenerator.generateCacheKey(ClazzAttendanceCount.class, id))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> clazzAttendanceCount)
                    .execute();
        }
        return clazzAttendanceCount;
    }

    private MongoConnection calculateMongoConnection(Long schoolId) {
        String mockId = ClazzAttendanceCount.generateId(schoolId, 1000L);
        MongoNamespace mongoNamespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(mongoNamespace);
    }

    private String getDay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return formatter.format(LocalDateTime.now());
    }
}
