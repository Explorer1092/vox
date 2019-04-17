package com.voxlearning.utopia.service.newexam.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamCounty;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/10/19
 */

@Named
@CacheBean(type = RptMockNewExamCounty.class, useValueWrapper = true, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class RptMockNewExamCountyDao extends AlpsStaticMongoDao<RptMockNewExamCounty, String> {
    @Override
    protected void calculateCacheDimensions(RptMockNewExamCounty document, Collection<String> dimensions) {
        dimensions.add(RptMockNewExamCounty.ckId(document.getId()));
    }

    @CacheMethod
    public List<RptMockNewExamCounty> loadRegions(String examId, Integer cityId) {
        Criteria criteria = Criteria
                .where("exam_id").is(examId)
                .and("city_id").is(cityId);
        return query(Query.query(criteria));
    }

}
