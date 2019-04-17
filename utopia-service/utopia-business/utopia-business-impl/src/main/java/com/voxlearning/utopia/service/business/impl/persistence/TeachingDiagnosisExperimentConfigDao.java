package com.voxlearning.utopia.service.business.impl.persistence;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;

import javax.inject.Named;
import java.util.*;


/**
 * 教学诊断实验配置
 * @Author songtao
 */
@Named
@CacheBean(type = TeachingDiagnosisExperimentConfig.class)
public class TeachingDiagnosisExperimentConfigDao extends AlpsStaticMongoDao<TeachingDiagnosisExperimentConfig, String> {

    @Override
    protected void calculateCacheDimensions(TeachingDiagnosisExperimentConfig document, Collection<String> dimensions) {
        dimensions.add(TeachingDiagnosisExperimentConfig.ck_id(document.getId()));
        dimensions.add(TeachingDiagnosisExperimentConfig.ck_group_id(document.getGroupId()));
    }

    public void updateStatus(String id, TeachingDiagnosisExperimentConfig.Status status) {
        Criteria criteria = Criteria.where("_id").is(id)
                .and("disabled").is(false)
                .and("status").ne(status.name());
        Update update = new Update().set("status", status.name())
                .set("updateTime", new Date());
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0L) {
            cleanCacheById(id);
        }
    }

    public void updateContent(TeachingDiagnosisExperimentConfig content) {
        Criteria criteria = Criteria.where("_id").is(content.getId())
                .and("disabled").is(false);
        Update update = new Update().set("updateTime", new Date());
        if (CollectionUtils.isNotEmpty(content.getLabels())) {
            update.set("labels", content.getLabels());
        }

        if (CollectionUtils.isNotEmpty(content.getGrades())) {
            update.set("grades", content.getGrades());
        }

        if (CollectionUtils.isNotEmpty(content.getRegions())) {
            update.set("regions", content.getRegions());
        }

        if (StringUtils.isNotBlank(content.getPreBonusDescription())) {
            update.set("beginning_prompt_words", content.getPreBonusDescription());
        }

        if (StringUtils.isNotBlank(content.getPreQuestion())) {
            update.set("doc_id", content.getPreQuestion());
        }

        if (StringUtils.isNotBlank(content.getPostQuestion())) {
            update.set("post_doc_id", content.getPostQuestion());
        }

        if (content.getBonus() != null) {
            update.set("bonus", content.getBonus());
        }

        if (CollectionUtils.isNotEmpty(content.getDiagnoses())) {
            update.set("diagnoses", content.getDiagnoses());
        }

        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0L) {
            cleanCacheById(content.getId());
        }
    }

    @CacheMethod
    public List<TeachingDiagnosisExperimentConfig> findByGroupId(@CacheParameter("GROUP") String groupId) {
        Criteria criteria = Criteria.where("group_id").is(groupId).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    public void deleteById(String id) {
        Criteria criteria = Criteria.where("_id").is(id)
                .and("disabled").is(false);
        Update update = new Update().set("disabled", true)
                .set("updateTime", new Date());
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0L) {
            cleanCacheById(id);
        }
    }

    private void cleanCacheById(String id) {
        TeachingDiagnosisExperimentConfig config = load(id);
        if (config != null) {
            Set<String> cacheIds = new HashSet<>();
            cacheIds.add(TeachingDiagnosisExperimentConfig.ck_id(config.getId()));
            cacheIds.add(TeachingDiagnosisExperimentConfig.ck_group_id(config.getGroupId()));
            getCache().deletes(cacheIds);
        }
    }
}
