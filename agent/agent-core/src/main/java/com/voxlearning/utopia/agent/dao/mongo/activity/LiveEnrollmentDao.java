package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollment;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LiveEnrollmentDao
 *
 * @author song.wang
 * @date 2018/12/17
 */
@Named
@CacheBean(type =  LiveEnrollment.class)
public class LiveEnrollmentDao extends StaticCacheDimensionDocumentMongoDao<LiveEnrollment, String> {

    public List<LiveEnrollment> loadByUserId(Long userId, Date startDate, Date endDate){
        if(startDate == null){
            startDate = DateUtils.addMonths(new Date(), -3);
        }
        Criteria criteria = Criteria.where("userId").is(userId);
        criteria.and("workTime").gte(startDate);
        if(endDate != null){
            criteria.lt(endDate);
        }
        return query(Query.query(criteria));
    }

    public List<LiveEnrollment> loadBySchoolId(Long schoolId, Date startDate, Date endDate){
        if(startDate == null){
            startDate = DateUtils.addMonths(new Date(), -3);
        }
        Criteria criteria = Criteria.where("schoolId").is(schoolId);
        criteria.and("workTime").gte(startDate);
        if(endDate != null){
            criteria.lt(endDate);
        }
        return query(Query.query(criteria));
    }

    public Map<Long,List<LiveEnrollment>> loadByUserIds(Collection<Long> userIds, Date startDate, Date endDate){
        if(CollectionUtils.isEmpty(userIds)){
            return Collections.emptyMap();
        }
        if(startDate == null){
            startDate = DateUtils.addMonths(new Date(), -3);
        }
        Criteria criteria = Criteria.where("userId").in(userIds);
        criteria.and("workTime").gte(startDate);
        if(endDate != null){
            criteria.lt(endDate);
        }
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(LiveEnrollment::getUserId));
    }

    public Map<Long,List<LiveEnrollment>> loadBySchoolIds(Collection<Long> schoolIds, Date startDate, Date endDate){
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        if(startDate == null){
            startDate = DateUtils.addMonths(new Date(), -3);
        }
        Criteria criteria = Criteria.where("schoolId").in(schoolIds);
        criteria.and("workTime").gte(startDate);
        if(endDate != null){
            criteria.lt(endDate);
        }
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(LiveEnrollment::getSchoolId));
    }

}
