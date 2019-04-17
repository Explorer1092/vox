package com.voxlearning.utopia.service.business.impl.persistence;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.DiagnosisExperimentLog;

import javax.inject.Named;
import java.util.*;


/**
 *
 */
@Named
@CacheBean(type = DiagnosisExperimentLog.class)
public class DiagnosisExperimentLogDao extends AlpsStaticMongoDao<DiagnosisExperimentLog, String> {

    @Override
    protected void calculateCacheDimensions(DiagnosisExperimentLog document, Collection<String> dimensions) {
        dimensions.add(DiagnosisExperimentLog.ck_id(document.getId()));
        dimensions.add(DiagnosisExperimentLog.ck_exp_id(document.getExperimentId()));
    }

    @CacheMethod
    public List<DiagnosisExperimentLog> findByExperimentId(@CacheParameter("EXP") String experimentId) {
        Criteria criteria = Criteria.where("experimentId")
                .is(experimentId)
                .and("disabled").is(false);
        return query(Query.query(criteria));
    }
}
