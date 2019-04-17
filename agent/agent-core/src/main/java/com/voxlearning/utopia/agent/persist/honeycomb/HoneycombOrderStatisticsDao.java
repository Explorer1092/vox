package com.voxlearning.utopia.agent.persist.honeycomb;

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
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombOrderStatistics;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@CacheBean(type = HoneycombOrderStatistics.class)
public class HoneycombOrderStatisticsDao extends StaticCacheDimensionDocumentMongoDao<HoneycombOrderStatistics, String> {

    @Inject
    private AgentCacheSystem agentCacheSystem;

    @CacheMethod
    public HoneycombOrderStatistics loadByUidAndDay(@CacheParameter("uid") Long honeycombId, @CacheParameter("d") Integer day, @CacheParameter("aid") String activityId){
        Criteria criteria = Criteria.where("honeycombId").is(honeycombId).and("day").is(day).and("activityId").is(activityId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }


    public List<HoneycombOrderStatistics> loadByUsersAndDays(String activityId, Collection<Long> honeycombIds, Collection<Integer> days){
        if(StringUtils.isBlank(activityId) || CollectionUtils.isEmpty(honeycombIds) || CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }

        Map<String, String> keyUserDayMap = new HashMap<>();
        List<String> keys = new ArrayList<>();
        for(Long userId : honeycombIds){
            for(Integer day : days){
                String key = HoneycombOrderStatistics.ck_uid_d_aid(userId, day, activityId);
                keys.add(key);
                keyUserDayMap.put(key, userId + "_" + day);
            }
        }

        List<HoneycombOrderStatistics> resultList = new ArrayList<>();
        Map<String, HoneycombOrderStatistics> cacheDataMap = agentCacheSystem.CBS.flushable.loads(keys);
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

        List<HoneycombOrderStatistics> uncachedDataList = new ArrayList<>();
        if(MapUtils.isNotEmpty(uncachedUserDayMap)){
            uncachedUserDayMap.forEach((k, v) -> {
                uncachedDataList.addAll(loadByUserAndDays(activityId, k, v));
            });
        }
        if(CollectionUtils.isNotEmpty(uncachedDataList)){
            resultList.addAll(uncachedDataList);
            uncachedDataList.forEach(p -> {
                agentCacheSystem.CBS.flushable.set(HoneycombOrderStatistics.ck_uid_d_aid(p.getHoneycombId(), p.getDay(), p.getActivityId()), SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), p);
            });
        }

        return resultList;
    }


    private List<HoneycombOrderStatistics> loadByUserAndDays(String activityId, Long honeycombId, Collection<Integer> days){
        if(StringUtils.isBlank(activityId) || CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("honeycombId").is(honeycombId).and("day").in(days).and("activityId").is(activityId);
        return query(Query.query(criteria));
    }


    private List<HoneycombOrderStatistics> loadByUserAndDays(Long honeycombId, Collection<Integer> days){
        if(CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("honeycombId").is(honeycombId).and("day").in(days);
        return query(Query.query(criteria));
    }




    public List<HoneycombOrderStatistics> loadByUsersAndDays(Collection<Long> honeycombIds, Collection<Integer> days){
        if(CollectionUtils.isEmpty(honeycombIds) || CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }

        Map<String, String> keyUserDayMap = new HashMap<>();
        List<String> keys = new ArrayList<>();
        for(Long userId : honeycombIds){
            for(Integer day : days){
                String key = HoneycombOrderStatistics.ck_uid_d(userId, day);
                keys.add(key);
                keyUserDayMap.put(key, userId + "_" + day);
            }
        }

        List<HoneycombOrderStatistics> resultList = new ArrayList<>();
        Map<String, List<HoneycombOrderStatistics>> cacheDataMap = agentCacheSystem.CBS.flushable.loads(keys);
        if(MapUtils.isNotEmpty(cacheDataMap)){
            List<HoneycombOrderStatistics> cacheList = cacheDataMap.values().stream().flatMap(List::stream).filter(Objects::nonNull).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(cacheList)){
                resultList.addAll(cacheList);
            }
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

        List<HoneycombOrderStatistics> uncachedDataList = new ArrayList<>();
        if(MapUtils.isNotEmpty(uncachedUserDayMap)){
            uncachedUserDayMap.forEach((k, v) -> {
                uncachedDataList.addAll(loadByUserAndDays(k, v));
            });
        }
        if(CollectionUtils.isNotEmpty(uncachedDataList)){
            resultList.addAll(uncachedDataList);
            Map<String, List<HoneycombOrderStatistics>> groupedDataMap = uncachedDataList.stream().collect(Collectors.groupingBy(p -> StringUtils.join(p.getHoneycombId(), "_", p.getDay())));
            groupedDataMap.forEach((k, v) -> {
                String[] userDay = StringUtils.split(k,"_");
                Long uid = SafeConverter.toLong(userDay[0]);
                Integer day = SafeConverter.toInt(userDay[1]);
                agentCacheSystem.CBS.flushable.set(HoneycombOrderStatistics.ck_uid_d(uid, day), SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), v);
            });
        }

        return resultList;
    }


}
