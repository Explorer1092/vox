package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentAppSuggest;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 天玑建议DAO
 * Created by yagaung.wang on 2016/8/4.
 */
@Named
@CacheBean(type = AgentAppSuggest.class)
public class AgentAppSuggestDao extends AlpsStaticMongoDao<AgentAppSuggest, String> {
    @Override
    protected void calculateCacheDimensions(AgentAppSuggest document, Collection<String> dimensions) {
        dimensions.add(AgentAppSuggest.ck_proponentId(document.getProponentId()));
        dimensions.add(CacheKeyGenerator.generateCacheKey(AgentAppSuggest.class, "all"));
    }

    @CacheMethod(key = "all")
    public List<AgentAppSuggest> findAll() {
        return query();
    }

    @CacheMethod
    public List<AgentAppSuggest> findByDate(Date startDate, Date endDate) {
        Criteria criteria = Criteria.where("createTime").gte(startDate).lte(endDate);
        Query query = Query.query(criteria);
        return query(query);
    }


}
