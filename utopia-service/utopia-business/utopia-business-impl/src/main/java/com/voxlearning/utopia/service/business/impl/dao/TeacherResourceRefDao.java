package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceRef;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TeacherResourceRef.class)
public class TeacherResourceRefDao extends AlpsStaticMongoDao<TeacherResourceRef, String> {

    @Override
    protected void calculateCacheDimensions(TeacherResourceRef document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    public List<TeacherResourceRef> loadByUserId(Long teacherId) {
        Criteria criteria = Criteria.where("teacherId").is(teacherId);
        return query(Query.query(criteria));
    }

}
