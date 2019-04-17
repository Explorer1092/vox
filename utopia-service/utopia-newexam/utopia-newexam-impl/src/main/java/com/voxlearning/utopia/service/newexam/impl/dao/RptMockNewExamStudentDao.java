package com.voxlearning.utopia.service.newexam.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamStudent;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/10/19
 */

@Named
@CacheBean(type = RptMockNewExamStudent.class, useValueWrapper = true, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class RptMockNewExamStudentDao extends AlpsStaticMongoDao<RptMockNewExamStudent, String> {
    @Override
    protected void calculateCacheDimensions(RptMockNewExamStudent document, Collection<String> dimensions) {
        dimensions.add(RptMockNewExamStudent.ckId(document.getId()));
    }

    @CacheMethod
    public List<RptMockNewExamStudent> loadStudents(String examId) {
        Criteria criteria = Criteria
                .where("exam_id").is(examId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<RptMockNewExamStudent> loadGroupStudents(String examId, Integer clazzId) {
        Criteria criteria = Criteria.where("exam_id").is(examId).and("class_id").is(clazzId);
        return query(Query.query(criteria));
    }
}
