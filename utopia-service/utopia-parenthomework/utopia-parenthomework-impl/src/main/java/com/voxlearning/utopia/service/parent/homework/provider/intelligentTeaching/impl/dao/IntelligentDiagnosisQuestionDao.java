package com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.entity.IntelligentDiagnosisQuestion;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.entity.VariantRef;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 讲练测题目dao
 *
 * @author Wenlong Meng
 * @since Feb 16, 2019
 */
@Named
public class IntelligentDiagnosisQuestionDao extends AlpsStaticMongoDao<IntelligentDiagnosisQuestion, String> {

    //Local variables
    /**
     * qid缓存
     */
    LoadingCache<String, String> questionIdsCache = CacheBuilder.newBuilder().maximumSize(171717).refreshAfterWrite(7, TimeUnit.DAYS).build(
            new CacheLoader<String, String>() {
                @Override
                public String load(String id) {
                    return $loadQuestionId(id);
                }
            });

    /**
     * zid缓存
     */
    LoadingCache<String, String> zIdsCache = CacheBuilder.newBuilder().maximumSize(171717).refreshAfterWrite(7, TimeUnit.DAYS).build(
            new CacheLoader<String, String>() {
                @Override
                public String load(String id) {
                    return $loadZIdByQId(id);
                }
            });

    @Override
    protected void calculateCacheDimensions(IntelligentDiagnosisQuestion course, Collection<String> collection) {
    }

    /**
     * 根据讲练测题id获取题id
     *
     * @param iDQid 讲练测题id
     * @return
     */
    public String loadQuestionId(String iDQid) {
        return questionIdsCache.getUnchecked(iDQid);
    }


    /**
     * 根据讲练测题id获取题id
     *
     * @param qid 题id
     * @return
     */
    public String loadZIdByQId(String qid) {
        return zIdsCache.getUnchecked(qid);
    }

    /**
     * 根据课时id获取变式id
     *s
     * @return
     */
    private String $loadQuestionId(String iDQid) {
        return load(iDQid).getQuestionId();
    }


    /**
     * 根据课时id获取变式id
     *s
     * @param qid
     * @return
     */
    private String $loadZIdByQId(String qid) {
        Criteria criteria = Criteria.where("question_id").is(qid);
        Query query = Query.query(criteria).limit(1);
        List<IntelligentDiagnosisQuestion> ms = query(query);
        return ObjectUtils.anyBlank(ms) ? qid : ms.get(0).getId();
    }

}
