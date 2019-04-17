package com.voxlearning.utopia.agent.dao.mongo;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentPerformanceRanking;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AgentPerformanceRankingDao
 *
 * @author song.wang
 * @date 2016/7/18
 */
@Named
@CacheBean(type = AgentPerformanceRanking.class)
public class AgentPerformanceRankingDao extends AlpsStaticMongoDao<AgentPerformanceRanking, String> {
    @Override
    protected void calculateCacheDimensions(AgentPerformanceRanking source, Collection<String> dimensions) {
        dimensions.add(AgentPerformanceRanking.ck_uid_type_day(source.getUserId(), source.getType(), source.getDay()));
        dimensions.add(AgentPerformanceRanking.ck_type_day(source.getType(), source.getDay()));
    }

    @CacheMethod
    public AgentPerformanceRanking findByUserId(@CacheParameter("uid") Long userId, @CacheParameter("type")Integer type, @CacheParameter("day")Integer day){
        Criteria criteria = Criteria.where("userId").is(userId).and("type").is(type).and("day").is(day);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public List<AgentPerformanceRanking> findByDay(@CacheParameter("type")Integer type, @CacheParameter("day")Integer day){
        Criteria criteria = Criteria.where("type").is(type).and("day").is(day);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public Map<Long, AgentPerformanceRanking> findByUserIds(@CacheParameter(value = "uid", multiple = true)List<Long> userIds, @CacheParameter("type")Integer type, @CacheParameter("day")Integer day){
        if(CollectionUtils.isEmpty(userIds)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("userId").in(userIds).and("type").is(type).and("day").is(day);
        Query query = Query.query(criteria);
        List<AgentPerformanceRanking> resultList = query(query);
        if(CollectionUtils.isEmpty(resultList)){
            return Collections.emptyMap();
        }
        return resultList.stream().collect(Collectors.toMap(AgentPerformanceRanking::getUserId, Function.identity()));
    }

    public List<AgentPerformanceRanking> findByUserName(Integer type, String userName, Integer day){
        Criteria criteria = Criteria.where("type").is(type).and("day").is(day).and("userName").regex(Pattern.compile(".*" + userName + ".*"));
        Query query = Query.query(criteria);
        return query(query);
    }

    public long deleteByDay(Integer day){
        Criteria criteria = Criteria.where("day").is(day);
        return $remove(Query.query(criteria));
    }
}
