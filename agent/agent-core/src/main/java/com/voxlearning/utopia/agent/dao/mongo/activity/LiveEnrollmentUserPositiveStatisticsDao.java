package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollmentUserPositiveStatistics;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@CacheBean(type = LiveEnrollmentUserPositiveStatistics.class)
public class LiveEnrollmentUserPositiveStatisticsDao extends StaticCacheDimensionDocumentMongoDao<LiveEnrollmentUserPositiveStatistics, String> {

    @CacheMethod
    public Map<Long, LiveEnrollmentUserPositiveStatistics> loadByUserIds(@CacheParameter(value = "uid",multiple = true) Collection<Long> userIds, @CacheParameter("d")Integer day){
        if(CollectionUtils.isEmpty(userIds) || day == null){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("userId").in(userIds).and("day").is(day);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(LiveEnrollmentUserPositiveStatistics::getUserId, Function.identity(), (o1, o2) -> o1));
    }

    public List<LiveEnrollmentUserPositiveStatistics> loadByUsersAndDays(Collection<Long> userIds, Collection<Integer> days){
        if(CollectionUtils.isEmpty(days) || CollectionUtils.isEmpty(userIds)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("userId").in(userIds).and("day").in(days);
        return query(Query.query(criteria));
    }
}
