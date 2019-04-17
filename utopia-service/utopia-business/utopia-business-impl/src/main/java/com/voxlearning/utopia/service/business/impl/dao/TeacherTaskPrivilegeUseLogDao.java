package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilegeUseLog;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Named
@CacheBean(type = TeacherTaskPrivilegeUseLog.class)
public class TeacherTaskPrivilegeUseLogDao extends AlpsStaticJdbcDao<TeacherTaskPrivilegeUseLog, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherTaskPrivilegeUseLog document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    public void removeByReceiveData(Date date) {
        Criteria criteria = Criteria.where("CREATE_DATETIME").lte(date);
        $remove(Query.query(criteria));
    }

}
