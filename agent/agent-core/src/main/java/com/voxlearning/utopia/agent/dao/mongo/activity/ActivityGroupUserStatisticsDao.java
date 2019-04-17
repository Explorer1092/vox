package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroupUserStatistics;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Named
@CacheBean(type =  ActivityGroupUserStatistics.class)
public class ActivityGroupUserStatisticsDao extends StaticCacheDimensionDocumentMongoDao<ActivityGroupUserStatistics, String> {

    @CacheMethod
    public ActivityGroupUserStatistics loadByUidAndDay(@CacheParameter("aid") String activityId, @CacheParameter("uid") Long userId, @CacheParameter("d") Integer day){
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId).and("day").is(day);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public List<ActivityGroupUserStatistics> loadByUsersAndDays(String activityId, Collection<Long> userIds, Collection<Integer> days){
        if(CollectionUtils.isEmpty(userIds) || CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").in(userIds).and("day").in(days);
        return query(Query.query(criteria));
    }

}
