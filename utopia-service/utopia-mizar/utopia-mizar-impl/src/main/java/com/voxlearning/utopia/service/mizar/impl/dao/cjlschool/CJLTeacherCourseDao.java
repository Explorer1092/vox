package com.voxlearning.utopia.service.mizar.impl.dao.cjlschool;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLDataDao;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacherCourse;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Yuechen.Wang on 2017/07/15.
 */
@Named
@CacheBean(type = CJLTeacherCourse.class)
public class CJLTeacherCourseDao extends StaticCacheDimensionDocumentMongoDao<CJLTeacherCourse, String> implements CJLDataDao<CJLTeacherCourse> {

    @Override
    public CJLTeacherCourse syncOne(CJLTeacherCourse entity) {
        entity.setSyncStatus(0);
        return upsert(entity);
    }

    @Override
    public void syncBatch(List<CJLTeacherCourse> entityList) {
        entityList.forEach(this::upsert);
    }

    @Override
    public List<CJLTeacherCourse> $findAllForJob() {
        return query();
    }

}
