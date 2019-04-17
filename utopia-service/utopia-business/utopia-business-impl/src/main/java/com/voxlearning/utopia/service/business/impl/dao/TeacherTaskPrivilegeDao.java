package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilege;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

@Named
@CacheBean(type = TeacherTaskPrivilege.class)
public class TeacherTaskPrivilegeDao extends AlpsStaticMongoDao<TeacherTaskPrivilege,Long>{

    @Override
    protected void calculateCacheDimensions(TeacherTaskPrivilege document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

}
