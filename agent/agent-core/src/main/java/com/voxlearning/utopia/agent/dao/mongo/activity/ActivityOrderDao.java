package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrder;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@CacheBean(type =  ActivityOrder.class)
public class ActivityOrderDao extends StaticCacheDimensionDocumentMongoDao<ActivityOrder, String> {

    @CacheMethod
    public ActivityOrder loadByOid(@CacheParameter("oid")String orderId){
        Criteria criteria = Criteria.where("orderId").is(orderId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<String, ActivityOrder> loadByOids(@CacheParameter(value = "oid", multiple = true) Collection<String> orderIds){
        Criteria criteria = Criteria.where("orderId").in(orderIds);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(ActivityOrder::getOrderId, Function.identity(), (o1, o2) -> o1));
    }


    public List<ActivityOrder> loadByActivityAndUserAndTime(String activityId, Collection<Long> userIds, Date startDate, Date endDate){
        if(StringUtils.isBlank(activityId) || CollectionUtils.isEmpty(userIds)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").in(userIds);
        if(startDate != null || endDate != null){
            criteria.and("orderPayTime");
            if(startDate != null){
                criteria.gte(startDate);
            }
            if(endDate != null){
                criteria.lt(endDate);
            }
        }
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<ActivityOrder> loadByAidAndUid(@CacheParameter("aid") String activityId, @CacheParameter("uid")Long userId){
        if(StringUtils.isBlank(activityId) || userId == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId);
        return query(Query.query(criteria));
    }

    public List<ActivityOrder> loadByAidAndOrderUserId(String activityId, Long orderUserId){
        if(StringUtils.isBlank(activityId) || orderUserId == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("orderUserId").is(orderUserId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<String,List<ActivityOrder>> loadByAidsAndUid(@CacheParameter(value = "aid",multiple = true) Collection<String> activityIds,@CacheParameter("uid") Long userId){
        if(CollectionUtils.isEmpty(activityIds) || userId == null){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("activityId").in(activityIds).and("userId").is(userId);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(ActivityOrder::getActivityId));
    }
}
