package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultPlan;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Named
@CacheBean(type = AIUserUnitResultPlan.class)
public class AIUserUnitResultPlanDao extends AsyncStaticMongoPersistence<AIUserUnitResultPlan, String> {

    @Override
    protected void calculateCacheDimensions(AIUserUnitResultPlan document, Collection<String> dimensions) {
        dimensions.add(AIUserUnitResultPlan.ck_id(document.getId()));
        dimensions.add(AIUserUnitResultPlan.ck_uid(document.getUserId()));
        dimensions.add(AIUserUnitResultPlan.ck_unit_id(document.getUnitId()));
    }

    public Map<String, AIUserUnitResultPlan> loadByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return loads(ids);
    }

    @CacheMethod
    public List<AIUserUnitResultPlan> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AIUserUnitResultPlan> loadByUnitId(@CacheParameter("UNID") String unitId) {
        Criteria criteria = Criteria.where("unitId").is(unitId);
        return query(Query.query(criteria));
    }
}
