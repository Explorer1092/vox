package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityControl;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;

@Named
@CacheBean(type =  ActivityControl.class)
public class ActivityControlDao extends StaticCacheDimensionDocumentMongoDao<ActivityControl, String> {

    @CacheMethod
    public List<ActivityControl> loadByAid(@CacheParameter("aid") String activityId){
        if(StringUtils.isBlank(activityId)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("disabled").is(false);
        return query(Query.query(criteria));
    }
}
