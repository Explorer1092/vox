package com.voxlearning.utopia.agent.dao.mongo.daily;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.daily.AgentDailyPlan;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentDailyPlan.class)
public class AgentDailyPlanDao extends StaticCacheDimensionDocumentMongoDao<AgentDailyPlan, String> {

    @CacheMethod
    public AgentDailyPlan loadByUserIdAndTime(@CacheParameter(value = "uid") Long userId,@CacheParameter(value = "time") Integer dailyTime){
        Criteria criteria = Criteria.where("userId").is(userId);
        criteria.and("dailyTime").is(dailyTime);
        criteria.and("disabled").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public AgentDailyPlan loadByDailyId(@CacheParameter(value = "did") String dailyId){
        Criteria criteria = Criteria.where("dailyId").is(dailyId);
        criteria.and("disabled").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long,AgentDailyPlan> loadByUserIdsAndTime(@CacheParameter(value = "uid",multiple = true) Collection<Long> userIds,@CacheParameter(value = "time") Integer dailyTime){
        Criteria criteria = Criteria.where("userId").in(userIds);
        criteria.and("dailyTime").is(dailyTime);
        criteria.and("disabled").is(false);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(AgentDailyPlan::getUserId, Function.identity(), (o1, o2) -> o1));
    }

}