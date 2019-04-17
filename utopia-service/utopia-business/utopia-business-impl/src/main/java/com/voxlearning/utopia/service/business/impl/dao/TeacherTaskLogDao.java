package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.task.TeacherTaskLog;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Named
@CacheBean(type = TeacherTaskLog.class)
public class TeacherTaskLogDao extends AlpsStaticJdbcDao<TeacherTaskLog, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherTaskLog document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    public void removeByReceiveData(Date date) {
        Criteria criteria = Criteria.where("RECEIVE_DATE").lte(date);
        $remove(Query.query(criteria));
    }

}
