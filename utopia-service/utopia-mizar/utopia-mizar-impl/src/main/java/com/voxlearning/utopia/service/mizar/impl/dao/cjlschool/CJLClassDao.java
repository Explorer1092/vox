package com.voxlearning.utopia.service.mizar.impl.dao.cjlschool;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLDataDao;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLClass;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacher;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Yuechen.Wang on 2017/07/15.
 */
@Named
@CacheBean(type = CJLClass.class)
public class CJLClassDao extends StaticCacheDimensionDocumentMongoDao<CJLClass, String> implements CJLDataDao<CJLClass> {

    @Override
    public CJLClass syncOne(CJLClass entity) {
        return upsert(entity);
    }

    @Override
    public void syncBatch(List<CJLClass> entityList) {
        entityList.forEach(this::upsert);
    }

    @Override
    public List<CJLClass> $findAllForJob() {
        return query();
    }

}
