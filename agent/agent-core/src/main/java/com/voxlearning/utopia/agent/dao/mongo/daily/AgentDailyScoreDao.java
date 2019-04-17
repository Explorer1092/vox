package com.voxlearning.utopia.agent.dao.mongo.daily;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.constants.AgentDailyScoreIndex;
import com.voxlearning.utopia.agent.persist.entity.daily.AgentDailyScore;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentDailyScore.class)
public class AgentDailyScoreDao extends StaticCacheDimensionDocumentMongoDao<AgentDailyScore, String> {

    @CacheMethod
    public List<AgentDailyScore> loadByDailyId(@CacheParameter(value = "did") String dailyId){
        Criteria criteria = Criteria.where("dailyId").is(dailyId);
        criteria.and("disabled").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public AgentDailyScore loadByUserIdAndTime(@CacheParameter(value = "uid")Long userId,@CacheParameter(value = "time") Integer dailyTime){
        Criteria criteria = Criteria.where("userId").is(userId);
        criteria.and("dailyTime").is(dailyTime);
        criteria.and("disabled").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public Map<Long,List<AgentDailyScore>> loadByUserIdsAndTime(Collection<Long> userIds, Integer startTime, Integer endTime){
        Criteria criteria = Criteria.where("userId").in(userIds);
        criteria.and("dailyTime").gte(startTime).lte(endTime);
        criteria.and("disabled").is(false);
        return  query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentDailyScore::getUserId));
    }

    @CacheMethod
    public Map<Long,AgentDailyScore> loadByUserIdsAndTimeAndIndex(@CacheParameter(value = "uid",multiple = true)Collection<Long> userIds, @CacheParameter(value = "time") Integer dailyTime, @CacheParameter(value = "index")AgentDailyScoreIndex index){
        Criteria criteria = Criteria.where("userId").in(userIds);
        criteria.and("dailyTime").is(dailyTime);
        criteria.and("index").is(index);
        criteria.and("disabled").is(false);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(AgentDailyScore::getUserId,Function.identity(),(o1,o2) -> o1));
    }
}