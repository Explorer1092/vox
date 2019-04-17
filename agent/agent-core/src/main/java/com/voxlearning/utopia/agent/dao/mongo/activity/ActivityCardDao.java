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
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCard;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@CacheBean(type =  ActivityCard.class)
public class ActivityCardDao extends StaticCacheDimensionDocumentMongoDao<ActivityCard, String> {

    @Inject
    private AgentCacheSystem agentCacheSystem;

    @CacheMethod
    public ActivityCard loadByCn(@CacheParameter("cn") String cardNo){
        Criteria criteria = Criteria.where("cardNo").is(cardNo);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public List<ActivityCard> loadByAidAndUid(@CacheParameter("aid") String activityId, @CacheParameter("uid")Long userId){
        if(StringUtils.isBlank(activityId) || userId == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId);
        return query(Query.query(criteria));
    }

    public List<ActivityCard> loadByAidAndUids(String activityId, Collection<Long> userIds){

        if(StringUtils.isBlank(activityId) || CollectionUtils.isEmpty(userIds)){
            return Collections.emptyList();
        }

        Map<String, Long> keyUserMap = new HashMap<>();
        List<String> keys = new ArrayList<>();
        for(Long userId : userIds){
            String key = ActivityCard.ck_aid_uid(activityId, userId);
            keys.add(key);
            keyUserMap.put(key, userId);
        }

        List<ActivityCard> resultList = new ArrayList<>();
        Map<String, List<ActivityCard>> cacheDataMap = agentCacheSystem.CBS.flushable.loads(keys);
        if(MapUtils.isNotEmpty(cacheDataMap)){
            for(List<ActivityCard> groupList : cacheDataMap.values()){
                resultList.addAll(groupList);
            }

        }

        List<Long> targetUserIds = new ArrayList<>();
        keyUserMap.forEach((k, u) -> {
            if(!cacheDataMap.containsKey(k)){
                targetUserIds.add(u);
            }
        });

        if(CollectionUtils.isNotEmpty(targetUserIds)){
            Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").in(targetUserIds);
            List<ActivityCard> dbList = query(Query.query(criteria));
            if(CollectionUtils.isNotEmpty(dbList)){
                resultList.addAll(dbList);
                Map<Long, List<ActivityCard>> userGroupMap = dbList.stream().collect(Collectors.groupingBy(ActivityCard::getUserId));
                userGroupMap.forEach((u, v) -> {
                    agentCacheSystem.CBS.flushable.set(ActivityCard.ck_aid_uid(activityId, u), SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), v);
                });
            }
        }
        return resultList;
    }

}
