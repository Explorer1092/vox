package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.task.TeacherRookieTask;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

@Named
@CacheBean(type = TeacherRookieTask.class, useValueWrapper = true)
public class TeacherRookieTaskDao extends AlpsStaticMongoDao<TeacherRookieTask, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherRookieTask document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod
    public TeacherRookieTask load(@CacheParameter("TID") Long teacherId, @CacheParameter("TYID") Long typeId) {
        Criteria criteria = Criteria.where("_id").is(teacherId).and("typeId").is(typeId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

}
