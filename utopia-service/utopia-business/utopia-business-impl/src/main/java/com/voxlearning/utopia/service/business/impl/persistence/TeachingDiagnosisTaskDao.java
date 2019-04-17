package com.voxlearning.utopia.service.business.impl.persistence;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisTask;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


/**
 *
 */
@Named
@CacheBean(type = TeachingDiagnosisTask.class)
public class TeachingDiagnosisTaskDao extends AlpsStaticMongoDao<TeachingDiagnosisTask, String> {

    @Override
    protected void calculateCacheDimensions(TeachingDiagnosisTask document, Collection<String> dimensions) {
        dimensions.add(TeachingDiagnosisTask.generateCacheKeyById(document.getId()));
        dimensions.add(TeachingDiagnosisTask.generateCacheKeyByUserId(document.getUserId()));
    }


    @CacheMethod
    public List<TeachingDiagnosisTask> findByUserId(@CacheParameter("UId") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(new Query(criteria));
    }
}
