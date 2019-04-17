package com.voxlearning.utopia.service.psr.impl.dao.newhomework;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.psr.entity.newhomework.KnowledgePointNew;
import com.voxlearning.utopia.service.psr.entity.newhomework.MathQuestionProfile;
import com.voxlearning.utopia.service.question.api.entity.EmbedSolutionMethodContent;
import com.voxlearning.utopia.service.question.api.entity.EmbedTestMethod;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/22
 * Time: 18:29
 * To change this template use File | Settings | File Templates.
 */

@Named
@UtopiaCacheSupport(MathQuestionProfile.class)
public class MathQuestionProfileDao extends AlpsStaticMongoDao<MathQuestionProfile,String> {
    @Override
    protected void calculateCacheDimensions(MathQuestionProfile source, Collection<String> dimensions){
    }

    @UtopiaCacheable
    public  List<MathQuestionProfile> getSimQuestionProfileByQuestion(MathQuestionProfile profile){
        List<MathQuestionProfile> mathQuestionProfiles = Lists.newArrayList();
        if (profile == null) return mathQuestionProfiles;
        List<String> kpfs = Lists.newArrayList();
        if (profile.getKnowledgePointNews() != null) {
            kpfs.addAll(profile.getKnowledgePointNews().stream().map(KnowledgePointNew::getKpf_id).collect(Collectors.toList()));
        }
//        kpfs.add("KP_10200051621682");
//        kpfs.add("KP_10200051315507");
        Criteria kpCriteria = null;
        if (!kpfs.isEmpty()) {
            kpCriteria = Criteria.where("knowledge_points_new.kpf_id").in(kpfs).and("deleted_at").exists(false);
        }
        List<String> tms = Lists.newArrayList();
        if (profile.getTestMethods() != null) {
            tms.addAll(profile.getTestMethods().stream().map(EmbedTestMethod::getId).collect(Collectors.toList()));
        }
//        tms.addAll(Lists.newArrayList("TM_10200000004515", "TM_10200000002654"));
        Criteria tmCriteria = null;
        if (!tms.isEmpty()) {
            tmCriteria = Criteria.where("test_methods.id").in(tms).and("deleted_at").exists(false);
        }

        List<String> sms = Lists.newArrayList();
        if (profile.getSolutionMethods() != null) {
            sms.addAll(profile.getSolutionMethods().stream().map(EmbedSolutionMethodContent::getId).collect(Collectors.toList()));
        }
//        sms.add("SM_10200000018586");
        Criteria smCriteria = null;
        if (!sms.isEmpty()) {
            smCriteria = Criteria.where("solution_methods.id").in(sms).and("deleted_at").exists(false);
        }
        Criteria queryCriteria = null;
        List<Criteria> filters = Lists.newArrayList();
        if (kpCriteria != null) filters.add(kpCriteria);
        if (tmCriteria != null) filters.add(tmCriteria);
        if (smCriteria != null) filters.add(smCriteria);

        if (filters.size() == 1) queryCriteria = filters.get(0);
        else if (filters.size() == 2) queryCriteria = Criteria.or(filters.get(0),filters.get(1));
        else if (filters.size() == 3) queryCriteria = Criteria.or(filters.get(0),filters.get(1),filters.get(2));

//        queryCriteria = Criteria.or(kpCriteria,tmCriteria,smCriteria);

        if (queryCriteria == null) return mathQuestionProfiles;
        mathQuestionProfiles.addAll(query(Query.query(queryCriteria)).stream().collect(Collectors.toList()));
        return mathQuestionProfiles;
    }

    public Map<String, MathQuestionProfile> getMathQuestionProfilesByDocIds(Collection<String> docIds) {
        if (CollectionUtils.isEmpty(docIds)) {
            return Collections.emptyMap();
        }

        Criteria criteria = Criteria.where("doc_id").in(docIds).and("deleted_at").exists(false);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(MathQuestionProfile::getDoc_id, Function.identity()));
    }

    public  List<MathQuestionProfile> getSimQuestionProfileByKp(String kp){
        List<MathQuestionProfile> mathQuestionProfiles = Lists.newArrayList();
        Criteria criteria = null;

        if (kp.startsWith("KP")) criteria = Criteria.where("knowledge_points_new.kpf_id").is(kp).and("deleted_at").exists(false);
        else if (kp.startsWith("TM")) criteria = Criteria.where("test_methods.id").is(kp).and("deleted_at").exists(false);
        else if (kp.startsWith("SM")) criteria = Criteria.where("solution_methods.id").is(kp).and("deleted_at").exists(false);
        if (criteria == null) return  mathQuestionProfiles;
        mathQuestionProfiles.addAll(query(Query.query(criteria)).stream().collect(Collectors.toList()));
        return mathQuestionProfiles;

    }
}

