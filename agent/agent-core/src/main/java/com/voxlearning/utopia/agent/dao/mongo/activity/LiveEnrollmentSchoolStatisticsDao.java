package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollmentSchoolStatistics;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@CacheBean(type =  LiveEnrollmentSchoolStatistics.class)
public class LiveEnrollmentSchoolStatisticsDao extends StaticCacheDimensionDocumentMongoDao<LiveEnrollmentSchoolStatistics, String> {

    @CacheMethod
    public Map<Long, LiveEnrollmentSchoolStatistics> loadBySchoolIds(@CacheParameter(value = "sid",multiple = true) Collection<Long> schoolIds, @CacheParameter("d")Integer day){
        if(CollectionUtils.isEmpty(schoolIds) || day == null){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("day").is(day).and("schoolId").in(schoolIds);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(LiveEnrollmentSchoolStatistics::getSchoolId, Function.identity(), (o1, o2) -> o1));
    }


    public List<LiveEnrollmentSchoolStatistics> loadByDays(Collection<Integer> days){
        if(CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("day").in(days);
        return query(Query.query(criteria));
    }


}
