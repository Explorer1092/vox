package com.voxlearning.utopia.service.mizar.impl.dao.microcourse;

import com.mongodb.WriteConcern;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriodRef;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * 微课堂-课程 DAO
 * Created by Yuechen.Wang on 2016/12/08.
 */
@Named
@CacheBean(type = MicroCoursePeriodRef.class)
public class MicroCoursePeriodRefDao extends StaticCacheDimensionDocumentMongoDao<MicroCoursePeriodRef, String> {

    @CacheMethod
    public List<MicroCoursePeriodRef> findByCourse(@CacheParameter("C") String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("courseId").is(courseId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public MicroCoursePeriodRef findByPeriod(@CacheParameter("P") String periodId) {
        return query(Query.query(Criteria.where("periodId").is(periodId)))
                .stream()
                .sorted((r1, r2) -> r2.getUpdateTime().compareTo(r1.getUpdateTime()))
                .findFirst()
                .orElse(null);
    }

    public boolean insertSpecificRef(String courseId, String periodId) {
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(periodId)) {
            return false;
        }
        MicroCoursePeriodRef ref = new MicroCoursePeriodRef();
        ref.setCourseId(courseId);
        ref.setPeriodId(periodId);
        insert(ref);
        return true;
    }

    public long removeCoursePeriod(String courseId, String periodId) {
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(periodId)) {
            return 0;
        }
        Criteria criteria = Criteria.where("courseId").is(courseId)
                .and("periodId").is(periodId);
        Bson filter = criteriaTranslator.translate(criteria);
        long cnt = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .deleteMany(filter)
                .getDeletedCount();
        if (cnt > 0) {
            MicroCoursePeriodRef ref = new MicroCoursePeriodRef();
            ref.setCourseId(courseId);
            ref.setPeriodId(periodId);
            evictDocumentCache(ref);
        }
        return cnt;
    }

}
