package com.voxlearning.utopia.service.mizar.impl.dao.microcourse;

import com.mongodb.WriteConcern;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourseUserRef;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * 微课堂-课程 DAO
 * Created by Yuechen.Wang on 2016/12/08.
 */
@Named
@CacheBean(type = MicroCourseUserRef.class)
public class MicroCourseUserRefDao extends StaticCacheDimensionDocumentMongoDao<MicroCourseUserRef, String> {

    @CacheMethod
    public List<MicroCourseUserRef> findByUser(@CacheParameter("U") String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<MicroCourseUserRef> findByCourse(@CacheParameter("C") String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("courseId").is(courseId);
        return query(Query.query(criteria));
    }

    public boolean insertSpecificRef(String courseId, String userId, MicroCourseUserRef.CourseUserRole role) {
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(userId) || role == null) {
            return false;
        }
        Criteria criteria = Criteria.where("courseId").is(courseId)
                .and("userId").is(userId);
        Bson filter = criteriaTranslator.translate(criteria);
        boolean exists = createMongoConnection().collection.count(filter) > 0;
        if (exists) {
            return false;
        }
        MicroCourseUserRef ref = new MicroCourseUserRef();
        ref.setCourseId(courseId);
        ref.setUserId(userId);
        ref.setRole(role);
        insert(ref);
        return true;
    }

    public long removeByCourse(String courseId, MicroCourseUserRef.CourseUserRole role) {
        if (StringUtils.isBlank(courseId) || role == null) {
            return 0;
        }
        Criteria criteria = Criteria.where("courseId").is(courseId)
                .and("role").is(role);
        Bson filter = criteriaTranslator.translate(criteria);
        return createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .deleteMany(filter)
                .getDeletedCount();
    }
}
