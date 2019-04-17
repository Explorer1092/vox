package com.voxlearning.utopia.service.mizar.impl.dao.cjlschool;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLDataDao;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLStudent;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Yuechen.Wang on 2017/07/15.
 */
@Named
@CacheBean(type = CJLStudent.class)
public class CJLStudentDao extends StaticCacheDimensionDocumentMongoDao<CJLStudent, String> implements CJLDataDao<CJLStudent> {

    @Override
    public CJLStudent syncOne(CJLStudent entity) {
        return upsert(entity);
    }

    @Override
    public void syncBatch(List<CJLStudent> entityList) {
        entityList.forEach(this::upsert);
    }

    @Override
    public List<CJLStudent> $findAllForJob() {
        return query();
    }

}
