package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.constants.AgentPerformanceStatisticsType;
import com.voxlearning.utopia.agent.persist.entity.AgentPerformanceStatistics;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AgentPerformanceStatisticsPersistence
 *
 * @author song.wang
 * @date 2017/3/27
 */
@Named
@CacheBean(type = AgentPerformanceStatistics.class)
public class AgentPerformanceStatisticsPersistence extends AlpsStaticJdbcDao<AgentPerformanceStatistics, Long> {
    @Override
    protected void calculateCacheDimensions(AgentPerformanceStatistics document, Collection<String> dimensions) {
        dimensions.add(AgentPerformanceStatistics.ck_uid(document.getUserId()));
        dimensions.add(AgentPerformanceStatistics.ck_uid_month(document.getUserId(), document.getMonth()));
        dimensions.add(AgentPerformanceStatistics.ck_gid_month(document.getGroupId(), document.getMonth()));
    }

    @UtopiaCacheable
    public List<AgentPerformanceStatistics> findByUserId(@UtopiaCacheKey(name = "uid")Long id){
        Criteria criteria = Criteria.where("STATISTICS_TYPE").is(AgentPerformanceStatisticsType.USER);
        criteria.and("USER_ID").is(id);
        return query(Query.query(criteria));
    }


    @UtopiaCacheable
    public Map<Long, List<AgentPerformanceStatistics>> findByUserIdsAndMonth(@UtopiaCacheKey(name = "uid", multiple=true)Collection<Long> userIds, @UtopiaCacheKey(name = "month")Integer month){
        Criteria criteria = Criteria.where("STATISTICS_TYPE").is(AgentPerformanceStatisticsType.USER);
        criteria.and("USER_ID").in(userIds);
        criteria.and("MONTH").is(month);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentPerformanceStatistics::getUserId));
    }


    @UtopiaCacheable
    public Map<Long, List<AgentPerformanceStatistics>> findByGroupIdsAndMonth(@UtopiaCacheKey(name = "gid", multiple=true)Collection<Long> groupIds, @UtopiaCacheKey(name = "month")Integer month){
        Criteria criteria = Criteria.where("STATISTICS_TYPE").is(AgentPerformanceStatisticsType.GROUP);
        criteria.and("GROUP_ID").in(groupIds);
        criteria.and("MONTH").is(month);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentPerformanceStatistics::getGroupId));
    }

}
