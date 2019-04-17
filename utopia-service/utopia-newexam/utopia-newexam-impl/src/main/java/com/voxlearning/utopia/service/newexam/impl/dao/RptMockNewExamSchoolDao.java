package com.voxlearning.utopia.service.newexam.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamCounty;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/10/19
 */

@Named
@CacheBean(type = RptMockNewExamSchool.class, useValueWrapper = true, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class RptMockNewExamSchoolDao extends AlpsStaticMongoDao<RptMockNewExamSchool, String> {
    @Override
    protected void calculateCacheDimensions(RptMockNewExamSchool document, Collection<String> dimensions) {
        dimensions.add(RptMockNewExamSchool.ckId(document.getId()));
    }

    @CacheMethod
    public List<RptMockNewExamSchool> loadSchools(String examId, Integer countyId) {
        Criteria criteria = Criteria
                .where("exam_id").is(examId)
                .and("county_id").is(countyId);
        return query(Query.query(criteria));
    }
}
