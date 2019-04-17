package com.voxlearning.utopia.service.business.impl.persistence;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisCourseResult;

import javax.inject.Named;
import java.util.Collection;


/**
 *
 */
@Named
@CacheBean(type = TeachingDiagnosisCourseResult.class)
public class TeachingDiagnosisCourseResultDao extends AlpsStaticMongoDao<TeachingDiagnosisCourseResult, String> {

    @Override
    protected void calculateCacheDimensions(TeachingDiagnosisCourseResult document, Collection<String> dimensions) {
        dimensions.add(TeachingDiagnosisCourseResult.generateCacheKeyById(document.getId()));
    }
}
