package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.business.api.entity.KnowledgeAbilityAnalysis;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author fugui.chang
 * @since 2016/9/27
 */
@Named
@CacheBean(type = KnowledgeAbilityAnalysis.class)
public class KnowledgeAbilityAnalysisDao extends AlpsStaticMongoDao<KnowledgeAbilityAnalysis,String> {

    @Override
    protected void calculateCacheDimensions(KnowledgeAbilityAnalysis document, Collection<String> dimensions) {
        dimensions.add(KnowledgeAbilityAnalysis.generateCacheKey(document.getSchoolId(),document.getYearmonth(),document.getSubject()));
    }


    public List<KnowledgeAbilityAnalysis> loadBySchoolIdSubjectDt(Long schoolId,String subject,Long beginDt,Long endDt) {
        Criteria criteria = Criteria.where("school_id").is(schoolId)
                .and("dt").gte(beginDt).lte(endDt)
                .and("subject").is(subject);
        return query(Query.query(criteria));
    }

}
