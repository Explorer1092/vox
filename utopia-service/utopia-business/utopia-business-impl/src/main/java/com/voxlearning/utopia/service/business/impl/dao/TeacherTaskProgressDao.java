package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TeacherTaskProgress.class)
public class TeacherTaskProgressDao extends AlpsStaticMongoDao<TeacherTaskProgress,String>{

    @Override
    protected void calculateCacheDimensions(TeacherTaskProgress document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod
    public List<TeacherTaskProgress> loadTeacherProgress(@CacheParameter("TEACHER_ID") Long teacherId) {
        Criteria criteria = Criteria.where("teacherId").is(teacherId);
        return query(Query.query(criteria));
    }

}
