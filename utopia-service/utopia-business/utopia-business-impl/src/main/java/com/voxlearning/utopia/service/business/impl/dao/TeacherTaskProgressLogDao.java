package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.task.TeacherTaskProgressLog;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TeacherTaskProgressLog.class)
public class TeacherTaskProgressLogDao extends AlpsStaticMongoDao<TeacherTaskProgressLog,String>{

    @Override
    protected void calculateCacheDimensions(TeacherTaskProgressLog document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    public void removeByReceiveTime(Long time) {
        Criteria criteria = Criteria.where("receiveTime").lte(time);
        $remove(Query.query(criteria));
    }

}
