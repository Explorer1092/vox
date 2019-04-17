package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.task.TeacherMonthTask;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

@Named
@CacheBean(type = TeacherMonthTask.class, useValueWrapper = true)
public class TeacherMonthTaskDao extends AlpsStaticMongoDao<TeacherMonthTask, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherMonthTask document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

}
