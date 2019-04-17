package com.voxlearning.utopia.agent.dao.mongo.daily;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.daily.AgentDaily;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentDaily.class)
public class AgentDailyDao extends StaticCacheDimensionDocumentMongoDao<AgentDaily, String> {

    @CacheMethod
    public AgentDaily loadByUserIdAndTime(@CacheParameter(value = "uid") Long userId,@CacheParameter(value = "time") Integer dailyTime){
        Criteria criteria = Criteria.where("dailyTime").is(dailyTime);
        criteria.and("userId").is(userId);
        criteria.and("disabled").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }


    @CacheMethod
    public Map<Long,AgentDaily> loadByUserIdsAndTime(@CacheParameter(value = "uid",multiple = true) Collection<Long> userIds,@CacheParameter(value = "time") Integer dailyTime){
        Criteria criteria = Criteria.where("dailyTime").is(dailyTime);
        criteria.and("userId").in(userIds);
        criteria.and("disabled").is(false);
        return  query(Query.query(criteria)).stream().collect(Collectors.toMap(AgentDaily::getUserId, Function.identity(), (o1, o2) -> o1));
    }
}