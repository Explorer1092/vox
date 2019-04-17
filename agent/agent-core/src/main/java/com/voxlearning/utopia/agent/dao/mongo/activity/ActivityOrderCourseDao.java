package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderCourse;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@CacheBean(type =  ActivityOrderCourse.class)
public class ActivityOrderCourseDao extends StaticCacheDimensionDocumentMongoDao<ActivityOrderCourse, String> {

    @CacheMethod
    public List<ActivityOrderCourse> loadBySidAndCid(@CacheParameter("sid") Long studentId, @CacheParameter("cid")String courseId){
        Criteria criteria = Criteria.where("studentId").is(studentId).and("courseId").is(courseId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<ActivityOrderCourse> loadByOid(@CacheParameter("oid") String orderId){
        Criteria criteria = Criteria.where("orderId").is(orderId);
        return query(Query.query(criteria));
    }


    @CacheMethod
    public Map<String, List<ActivityOrderCourse>> loadByOids(@CacheParameter(value = "oid", multiple = true) Collection<String> orderIds){
        Criteria criteria = Criteria.where("orderId").in(orderIds);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(ActivityOrderCourse::getOrderId));
    }


}
