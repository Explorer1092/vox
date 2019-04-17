package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentMonthly;

import javax.inject.Named;
import java.util.Collection;

/**
 * AgentMonthlyDao
 *
 * @author song.wang
 * @date 2016/8/17
 */
@Named
@CacheBean(type = AgentMonthly.class)
public class AgentMonthlyDao extends AlpsStaticMongoDao<AgentMonthly, String> {
    @Override
    protected void calculateCacheDimensions(AgentMonthly document, Collection<String> dimensions) {
        dimensions.add(AgentMonthly.ck_uid_month(document.getUserId(), document.getMonth()));
    }

    @CacheMethod
    public AgentMonthly findByUserAndMonth(@CacheParameter("uid") Long userId, @CacheParameter("month")Integer month){
        Criteria criteria = Criteria.where("userId").is(userId).and("month").is(month);
        Query query = new Query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    public long deleteByMonth(Integer month){
        Criteria criteria = Criteria.where("month").is(month);
        return $remove(Query.query(criteria));
    }
}
