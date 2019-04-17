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
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroup;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroupUser;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrder;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@CacheBean(type =  ActivityGroup.class)
public class ActivityGroupDao extends StaticCacheDimensionDocumentMongoDao<ActivityGroup, String> {

    @Inject
    private AgentCacheSystem agentCacheSystem;

    @CacheMethod
    public ActivityGroup loadByGid(@CacheParameter("gid")String groupId){
        Criteria criteria = Criteria.where("groupId").is(groupId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public List<ActivityGroup> loadByAidAndUids(String activityId, Collection<Long> userIds){

        if(StringUtils.isBlank(activityId) || CollectionUtils.isEmpty(userIds)){
            return Collections.emptyList();
        }

        Map<String, Long> keyUserMap = new HashMap<>();
        List<String> keys = new ArrayList<>();
        for(Long userId : userIds){
            String key = ActivityGroup.ck_aid_uid(activityId, userId);
            keys.add(key);
            keyUserMap.put(key, userId);
        }

        List<ActivityGroup> resultList = new ArrayList<>();
        Map<String, List<ActivityGroup>> cacheDataMap = agentCacheSystem.CBS.flushable.loads(keys);
        if(MapUtils.isNotEmpty(cacheDataMap)){
            for(List<ActivityGroup> groupList : cacheDataMap.values()){
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
            List<ActivityGroup> dbList = query(Query.query(criteria));
            if(CollectionUtils.isNotEmpty(dbList)){
                resultList.addAll(dbList);
                Map<Long, List<ActivityGroup>> userGroupMap = dbList.stream().collect(Collectors.groupingBy(ActivityGroup::getUserId));
                userGroupMap.forEach((u, v) -> {
                    agentCacheSystem.CBS.flushable.set(ActivityGroup.ck_aid_uid(activityId, u), SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), v);
                });
            }
        }
        return resultList;
    }

}
