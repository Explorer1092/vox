package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityExtend;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@CacheBean(type =  ActivityExtend.class)
public class ActivityExtendDao extends StaticCacheDimensionDocumentMongoDao<ActivityExtend, String> {


    @CacheMethod
    public ActivityExtend loadByAid(@CacheParameter("aid") String activityId){
        if(StringUtils.isBlank(activityId)){
            return null;
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("disabled").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<String, ActivityExtend> loadByAids(@CacheParameter(value = "aid",multiple = true) Collection<String> activityIds){
        if(CollectionUtils.isEmpty(activityIds)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("activityId").in(activityIds).and("disabled").is(false);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(ActivityExtend::getActivityId, Function.identity(), (o1, o2) -> o1));
    }
}
