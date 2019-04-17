package com.voxlearning.utopia.service.business.impl.persistence;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisPreviewQuestionResult;

import javax.inject.Named;
import java.util.Collection;


/**
 * 前测题答案
 */
@Named
@CacheBean(type = TeachingDiagnosisPreviewQuestionResult.class)
public class TeachingDiagnosisPreviewQuestionResultDao extends AlpsStaticMongoDao<TeachingDiagnosisPreviewQuestionResult, String> {

    @Override
    protected void calculateCacheDimensions(TeachingDiagnosisPreviewQuestionResult document, Collection<String> dimensions) {
        dimensions.add(TeachingDiagnosisPreviewQuestionResult.ck_id(document.getId()));
    }
}
