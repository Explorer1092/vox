package com.voxlearning.utopia.service.business.impl.persistence;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.ExperimentType;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentGroupConfig;

import javax.inject.Named;
import java.util.*;


/**
 * 教学诊断实验组配置
 * @Author songtao
 */
@Named
@CacheBean(type = TeachingDiagnosisExperimentGroupConfig.class)
public class TeachingDiagnosisExperimentGroupConfigDao extends AlpsStaticMongoDao<TeachingDiagnosisExperimentGroupConfig, String> {
    @Override
    protected void calculateCacheDimensions(TeachingDiagnosisExperimentGroupConfig document, Collection<String> dimensions) {
        dimensions.add(TeachingDiagnosisExperimentGroupConfig.ck_all(document.getGroupType()));
        dimensions.add(TeachingDiagnosisExperimentGroupConfig.ck_id(document.getId()));
    }

    @CacheMethod
    public List<TeachingDiagnosisExperimentGroupConfig> findAll(@CacheParameter("TYPE") ExperimentType type) {
        Criteria criteria = Criteria.where("disabled").is(false).and("groupType").is(type.name());
        return query(Query.query(criteria));
    }

    public void deleteById(String id) {
        Criteria criteria = Criteria.where("_id").is(id)
                .and("disabled").is(false);
        Update update = new Update().set("disabled", true)
                .set("updateTime", new Date());
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0) {
            cleanCacheById(id);
        }
    }

    private void cleanCacheById(String id) {
        TeachingDiagnosisExperimentGroupConfig config = load(id);
        if (config != null) {
            Set<String> cacheIds = new HashSet<>();
            cacheIds.add(TeachingDiagnosisExperimentGroupConfig.ck_id(config.getId()));
            cacheIds.add(TeachingDiagnosisExperimentGroupConfig.ck_all(config.getGroupType()));
            getCache().deletes(cacheIds);
        }
    }
}
