package com.voxlearning.utopia.service.business.impl.dao;

/**
 *
 * @author fugui.chang
 * @since 2016/9/26
 */

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.business.api.entity.ClassStudySitutation;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = ClassStudySitutation.class)
public class ClassStudySitutationDao extends AlpsStaticMongoDao<ClassStudySitutation,String> {
    @Override
    protected void calculateCacheDimensions(ClassStudySitutation document, Collection<String> dimensions) {
        dimensions.add(ClassStudySitutation.generateCacheKey(document.getSchoolId(),document.getYearmonth(),document.getSubject()));
    }

    public List<ClassStudySitutation> loadClassStudySitutationBySchoolIdDtSubject( Long schoolId,  Long dt,  String subject){
        Criteria criteria = Criteria.where("school_id").is(schoolId)
                .and("dt").is(dt)
                .and("subject").is(subject);
        return query(Query.query(criteria));
    }
}
