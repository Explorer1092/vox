package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderRef;

import javax.inject.Named;

@Named
@CacheBean(type =  ActivityOrderRef.class)
public class ActivityOrderRefDao extends StaticCacheDimensionDocumentMongoDao<ActivityOrderRef, String> {

    @CacheMethod
    public ActivityOrderRef loadByOid(@CacheParameter("oid")String orderId){
        Criteria criteria = Criteria.where("orderId").is(orderId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
}
