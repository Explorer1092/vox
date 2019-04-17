package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCardStatistics;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
@CacheBean(type =  ActivityCardStatistics.class)
public class ActivityCardStatisticsDao extends StaticCacheDimensionDocumentMongoDao<ActivityCardStatistics, String> {

    @Inject
    private AgentCacheSystem agentCacheSystem;

    @CacheMethod
    public ActivityCardStatistics loadByUidAndDay(@CacheParameter("aid") String activityId, @CacheParameter("uid") Long userId, @CacheParameter("d") Integer day){
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId).and("day").is(day);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public List<ActivityCardStatistics> loadByUsersAndDays(String activityId, Collection<Long> userIds, Collection<Integer> days){
        if(CollectionUtils.isEmpty(userIds) || CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }

        Map<String, String> keyUserDayMap = new HashMap<>();
        List<String> keys = new ArrayList<>();
        for(Long userId : userIds){
            for(Integer day : days){
                String key = ActivityCardStatistics.ck_aid_uid_d(activityId, userId, day);
                keys.add(key);
                keyUserDayMap.put(key, userId + "_" + day);
            }
        }

        List<ActivityCardStatistics> resultList = new ArrayList<>();
        Map<String, ActivityCardStatistics> cacheDataMap = agentCacheSystem.CBS.flushable.loads(keys);
        if(MapUtils.isNotEmpty(cacheDataMap)){
            resultList.addAll(cacheDataMap.values());
        }

        Map<Long, Set<Integer>> uncachedUserDayMap = new HashMap<>();

        keyUserDayMap.forEach((k, ud) -> {
            if(!cacheDataMap.containsKey(k)){
                String[] userDay = StringUtils.split(ud,"_");
                Long uid = SafeConverter.toLong(userDay[0]);
                Integer day = SafeConverter.toInt(userDay[1]);
                Set<Integer> tmpDays = uncachedUserDayMap.computeIfAbsent(uid, k1 -> new HashSet<>());
                tmpDays.add(day);
            }
        });

        List<ActivityCardStatistics> uncachedDataList = new ArrayList<>();
        if(MapUtils.isNotEmpty(uncachedUserDayMap)){
            uncachedUserDayMap.forEach((k, v) -> {
                uncachedDataList.addAll(loadByUserAndDays(activityId, k, v));
            });
        }
        if(CollectionUtils.isNotEmpty(uncachedDataList)){
            resultList.addAll(uncachedDataList);
            uncachedDataList.forEach(p -> {
                agentCacheSystem.CBS.flushable.set(ActivityCardStatistics.ck_aid_uid_d(p.getActivityId(), p.getUserId(), p.getDay()), SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), p);
            });
        }

        return resultList;
    }


    private List<ActivityCardStatistics> loadByUserAndDays(String activityId, Long userId, Collection<Integer> days){
        if(CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId).and("day").in(days);
        return query(Query.query(criteria));
    }
}
