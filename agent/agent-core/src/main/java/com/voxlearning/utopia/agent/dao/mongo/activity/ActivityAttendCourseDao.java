package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityAttendCourse;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;

@Named
@CacheBean(type =  ActivityAttendCourse.class)
public class ActivityAttendCourseDao extends StaticCacheDimensionDocumentMongoDao<ActivityAttendCourse, String> {


    @CacheMethod
    public List<ActivityAttendCourse> loadBySidAndCid(@CacheParameter("aid") String activityId, @CacheParameter("rid") String relatedId, @CacheParameter("sid")Long studentId, @CacheParameter("cid") String courseId){
        if(StringUtils.isBlank(activityId) || StringUtils.isBlank(relatedId) || StringUtils.isBlank(courseId) ||studentId == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("relatedId").is(relatedId).and("studentId").is(studentId).and("courseId").is(courseId);
        return query(Query.query(criteria));
    }


}
