package com.voxlearning.utopia.agent.dao.mongo.daily;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.daily.AgentDailyScoreStatistics;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentDailyScoreStatisticsDao
 *
 * @author deliang.che
 * @since  2018/11/23
 */
@Named
@CacheBean(type = AgentDailyScoreStatistics.class)
public class AgentDailyScoreStatisticsDao extends StaticCacheDimensionDocumentMongoDao<AgentDailyScoreStatistics, String> {

    @CacheMethod
    public Map<Long,AgentDailyScoreStatistics> getGroupDailyStatistics(@CacheParameter(value = "gid",multiple = true) Collection<Long> groupIds, @CacheParameter("d") Integer date, @CacheParameter("dt") Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Criteria criteria = Criteria.where("groupId").in(groupIds).and("date").is(date).and("dateType").is(dateType).and("groupOrUser").is(1).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.toMap(AgentDailyScoreStatistics::getGroupId, Function.identity(),(o1,o2) -> o2));
    }

    @CacheMethod
    public Map<Long,AgentDailyScoreStatistics> getUserDailyStatistics(@CacheParameter(value = "uid",multiple = true) Collection<Long> userIds, @CacheParameter("d") Integer date, @CacheParameter("dt") Integer dateType){
        if (CollectionUtils.isEmpty(userIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Criteria criteria = Criteria.where("userId").in(userIds).and("date").is(date).and("dateType").is(dateType).and("groupOrUser").is(2).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.toMap(AgentDailyScoreStatistics::getUserId, Function.identity(),(o1,o2) -> o2));
    }

    public void disableData(Collection<Long> ids, Integer date, Integer dateType, Integer groupOrUser){
        if (CollectionUtils.isEmpty(ids) || null == date || dateType == null || groupOrUser == null || (groupOrUser != 1 && groupOrUser != 2)) {
            return ;
        }

        Update update = Update.update("disabled", true);
        Criteria criteria = new Criteria();
        if(groupOrUser == 1){ // 部门
            criteria.and("groupId").in(ids);
        }else { // 用户
            criteria.and("userId").in(ids);
        }
        criteria.and("date").is(date).and("dateType").is(dateType).and("groupOrUser").is(groupOrUser).and("disabled").is(false);
        Query query = Query.query(criteria);
        List<AgentDailyScoreStatistics> list = query(query);
        executeUpdateMany(createMongoConnection(), criteria, update);
        evictDocumentCache(list);
    }
}
