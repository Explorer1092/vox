package com.voxlearning.utopia.service.newexam.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamClazz;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/10/19
 */

@Named
@CacheBean(type = RptMockNewExamClazz.class, useValueWrapper = true, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class RptMockNewExamClazzDao extends AlpsStaticMongoDao<RptMockNewExamClazz, String> {
    @Override
    protected void calculateCacheDimensions(RptMockNewExamClazz document, Collection<String> dimensions) {
        dimensions.add(RptMockNewExamClazz.ckId(document.getId()));

    }

    @CacheMethod
    public List<RptMockNewExamClazz> loadClasses(String examId, Integer schoolId) {
        Criteria criteria = Criteria
                .where("exam_id").is(examId)
                .and("school_id").is(schoolId);
        return query(Query.query(criteria));
    }
}
