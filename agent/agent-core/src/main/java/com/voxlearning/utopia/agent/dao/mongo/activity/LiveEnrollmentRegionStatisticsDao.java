package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollmentRegionStatistics;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Named
@CacheBean(type =  LiveEnrollmentRegionStatistics.class)
public class LiveEnrollmentRegionStatisticsDao extends StaticCacheDimensionDocumentMongoDao<LiveEnrollmentRegionStatistics, String> {

    @CacheMethod
    public LiveEnrollmentRegionStatistics loadByCountyCode(@CacheParameter(value = "county") Integer countyCode, @CacheParameter("d")Integer day){
        if(countyCode == null || day == null){
            return null;
        }
        Criteria criteria = Criteria.where("day").is(day).and("countyCode").is(countyCode);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public List<LiveEnrollmentRegionStatistics> loadByDays(Collection<Integer> days){
        if(CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("day").in(days);
        return query(Query.query(criteria));
    }
}
