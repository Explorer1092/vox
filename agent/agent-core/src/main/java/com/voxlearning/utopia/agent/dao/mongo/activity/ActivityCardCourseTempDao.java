package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCardCourseTemp;

import javax.inject.Named;

@Named
@CacheBean(type =  ActivityCardCourseTemp.class)
public class ActivityCardCourseTempDao extends StaticCacheDimensionDocumentMongoDao<ActivityCardCourseTemp, String> {

    @CacheMethod
    public ActivityCardCourseTemp loadByCn(@CacheParameter("cn") String cardNo){
        Criteria criteria = Criteria.where("cardNo").is(cardNo);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
}
