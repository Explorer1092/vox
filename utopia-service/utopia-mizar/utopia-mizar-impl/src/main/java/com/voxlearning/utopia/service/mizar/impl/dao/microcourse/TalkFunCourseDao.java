package com.voxlearning.utopia.service.mizar.impl.dao.microcourse;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.TalkFunCourse;

import javax.inject.Named;

/**
 * 微课堂-欢拓后台对应实体DAO
 * Created by Wang Yuechen on 2016/01/11.
 */
@Named
@CacheBean(type = TalkFunCourse.class)
public class TalkFunCourseDao extends StaticCacheDimensionDocumentMongoDao<TalkFunCourse, String> {

    public void updateCourseStatus(String courseId, TalkFunCourse.Status status) {
        if (StringUtils.isBlank(courseId)) {
            return;
        }
        Criteria criteria = Criteria.where("courseId").is(courseId);
        TalkFunCourse course = query(Query.query(criteria)).stream().findFirst().orElse(null);
        if (course == null) {
            return;
        }
        course.setCourseStatus(status.name());
        upsert(course);
    }
}
