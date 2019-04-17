package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCardCourse;

import javax.inject.Named;
import java.util.List;

@Named
@CacheBean(type =  ActivityCardCourse.class)
public class ActivityCardCourseDao extends StaticCacheDimensionDocumentMongoDao<ActivityCardCourse, String> {

    @CacheMethod
    public List<ActivityCardCourse> loadBySidAndCid(@CacheParameter("sid") Long studentId, @CacheParameter("cid")String courseId){
        Criteria criteria = Criteria.where("studentId").is(studentId).and("courseId").is(courseId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<ActivityCardCourse> loadByOid(@CacheParameter("cn") String cardNo){
        Criteria criteria = Criteria.where("cardNo").is(cardNo);
        return query(Query.query(criteria));
    }

}
