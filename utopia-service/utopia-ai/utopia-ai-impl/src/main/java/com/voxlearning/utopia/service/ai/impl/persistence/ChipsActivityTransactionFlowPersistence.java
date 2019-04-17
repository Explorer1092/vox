package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsActivityTransactionFlow;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


@Named
@CacheBean(type = ChipsActivityTransactionFlow.class)
public class ChipsActivityTransactionFlowPersistence extends StaticMySQLPersistence<ChipsActivityTransactionFlow, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsActivityTransactionFlow document, Collection<String> dimensions) {
        dimensions.add(ChipsActivityTransactionFlow.ck_id(document.getId()));
    }

    public List<ChipsActivityTransactionFlow> loadAll() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public List<ChipsActivityTransactionFlow> loadByUserId(Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        return query(Query.query(criteria));
    }

    public List<ChipsActivityTransactionFlow> loadByActivityTypeAndUserId(String activityType, Long userId) {
        Criteria criteria = Criteria.where("ACTIVITY_TYPE").is(activityType).and("USER_ID").is(userId);
        return query(Query.query(criteria));
    }

}
