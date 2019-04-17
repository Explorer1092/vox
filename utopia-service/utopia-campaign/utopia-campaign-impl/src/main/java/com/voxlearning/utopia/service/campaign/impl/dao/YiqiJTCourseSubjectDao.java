package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourseSubject;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class YiqiJTCourseSubjectDao extends AlpsStaticJdbcDao<YiqiJTCourseSubject,Long> {
    @Override
    protected void calculateCacheDimensions(YiqiJTCourseSubject document, Collection<String> dimensions) {

    }

    public List<YiqiJTCourseSubject> getCourseNotesByCourseId(long courseId) {
        List<YiqiJTCourseSubject> result = null;
        Criteria criteria = Criteria.where("COURSE_ID").is(courseId);
        return query(Query.query(criteria));
    }

    public void deleteByCourseId(long courseId) {
        $remove(Criteria.where("COURSE_ID").is(courseId));
    }
}
