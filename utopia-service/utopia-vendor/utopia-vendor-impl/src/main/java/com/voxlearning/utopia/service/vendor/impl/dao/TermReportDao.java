package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.vendor.api.entity.TermReport;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author malong
 * @since 2017/6/19
 */
@Named
@CacheBean(type = TermReport.class)
public class TermReportDao extends AlpsStaticMongoDao<TermReport, String> {
    @Override
    protected void calculateCacheDimensions(TermReport document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod
    public TermReport getTermReport(@CacheParameter(value = "PID") Long parentId, @CacheParameter(value = "SID") Long studentId) {
        Criteria criteria = Criteria.where("parent_id").is(parentId)
                .and("student_id").is(studentId);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }
}
