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
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombFansStatistics;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
@CacheBean(type = HoneycombFansStatistics.class)
public class HoneycombFansStatisticsDao extends StaticCacheDimensionDocumentMongoDao<HoneycombFansStatistics, String> {

    @Inject
    private AgentCacheSystem agentCacheSystem;

    @CacheMethod
    public HoneycombFansStatistics loadByUidAndDay(@CacheParameter("uid") Long honeycombId, @CacheParameter("d") Integer day){
        Criteria criteria = Criteria.where("honeycombId").is(honeycombId).and("day").is(day);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }


    private List<HoneycombFansStatistics> loadByUserAndDays(Long honeycombId, Collection<Integer> days){
        if(CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("honeycombId").is(honeycombId).and("day").in(days);
        return query(Query.query(criteria));
    }

    public List<HoneycombFansStatistics> loadByUsersAndDays(Collection<Long> honeycombIds, Collection<Integer> days){
        if(CollectionUtils.isEmpty(honeycombIds) || CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }

        Map<String, String> keyUserDayMap = new HashMap<>();
        List<String> keys = new ArrayList<>();
        for(Long userId : honeycombIds){
            for(Integer day : days){
                String key = HoneycombFansStatistics.ck_uid_d(userId, day);
                keys.add(key);
                keyUserDayMap.put(key, userId + "_" + day);
            }
        }

        List<HoneycombFansStatistics> resultList = new ArrayList<>();
        Map<String, HoneycombFansStatistics> cacheDataMap = agentCacheSystem.CBS.flushable.loads(keys);
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

        List<HoneycombFansStatistics> uncachedDataList = new ArrayList<>();
        if(MapUtils.isNotEmpty(uncachedUserDayMap)){
            uncachedUserDayMap.forEach((k, v) -> {
                uncachedDataList.addAll(loadByUserAndDays(k, v));
            });
        }
        if(CollectionUtils.isNotEmpty(uncachedDataList)){
            resultList.addAll(uncachedDataList);
            uncachedDataList.forEach(p -> {
                agentCacheSystem.CBS.flushable.set(HoneycombFansStatistics.ck_uid_d(p.getHoneycombId(), p.getDay()), SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), p);
            });
        }

        return resultList;
    }
}
