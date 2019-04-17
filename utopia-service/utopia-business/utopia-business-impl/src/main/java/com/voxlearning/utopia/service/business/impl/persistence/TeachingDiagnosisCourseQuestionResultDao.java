package com.voxlearning.utopia.service.business.impl.persistence;



import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisCourseQuestionResult;

import javax.inject.Named;
import java.util.*;
import java.util.regex.Pattern;


/**
 *
 */
@Named
@CacheBean(type = TeachingDiagnosisCourseQuestionResult.class)
public class TeachingDiagnosisCourseQuestionResultDao extends AlpsStaticMongoDao<TeachingDiagnosisCourseQuestionResult, String> {

    @Override
    protected void calculateCacheDimensions(TeachingDiagnosisCourseQuestionResult document, Collection<String> dimensions) {
        dimensions.add(TeachingDiagnosisCourseQuestionResult.ck_task(document.getTaskId()));
        dimensions.add(TeachingDiagnosisCourseQuestionResult.ck_id(document.getId()));
    }

    @CacheMethod
    public List<TeachingDiagnosisCourseQuestionResult> loadByTaskId(@CacheParameter("taskId")String taskId) {
        Criteria criteria = Criteria.where("taskId").is(taskId);
         return query(Query.query(criteria));
    }

}
