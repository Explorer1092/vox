package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.AIUserBookResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Named
@CacheBean(type = AIUserBookResult.class)
public class AIUserBookResultDao extends AsyncStaticMongoPersistence<AIUserBookResult, String> {

    @Override
    protected void calculateCacheDimensions(AIUserBookResult document, Collection<String> dimensions) {
        dimensions.add(AIUserBookResult.ck_id(document.getId()));
        dimensions.add(AIUserBookResult.ck_uid(document.getUserId()));
    }

    public Map<String, AIUserBookResult> loadByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return loads(ids);
    }

    @CacheMethod
    public List<AIUserBookResult> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }
}
