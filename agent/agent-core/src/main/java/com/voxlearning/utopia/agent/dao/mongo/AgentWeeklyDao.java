package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentWeekly;

import javax.inject.Named;
import java.util.Collection;

/**
 * AgentWeeklyDao
 *
 * @author song.wang
 * @date 2016/8/11
 */
@Named
@CacheBean(type = AgentWeekly.class)
public class AgentWeeklyDao extends AlpsStaticMongoDao<AgentWeekly, String> {
    @Override
    protected void calculateCacheDimensions(AgentWeekly document, Collection<String> dimensions) {
        dimensions.add(AgentWeekly.ck_uid_day(document.getUserId(), document.getDay()));
    }

    @CacheMethod
    public AgentWeekly findByUserAndDay(@CacheParameter("uid") Long userId, @CacheParameter("day")Integer day){
        Criteria criteria = Criteria.where("userId").is(userId).and("day").is(day);
        Query query = new Query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    public long deleteByDay(Integer day){
        Criteria criteria = Criteria.where("day").is(day);
        return $remove(Query.query(criteria));
    }


}
