package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.RemindAssignHomeworkTeacher;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = RemindAssignHomeworkTeacher.class)
public class RemindAssignHomeworkTeacherPersistence extends StaticMySQLPersistence<RemindAssignHomeworkTeacher, Long> {

    @Override
    protected void calculateCacheDimensions(RemindAssignHomeworkTeacher document, Collection<String> dimensions) {
        dimensions.add(RemindAssignHomeworkTeacher.ck_id(document.getId()));
    }
}
