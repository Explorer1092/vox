package com.voxlearning.utopia.agent.dao;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.agent.AgentPerformanceConfig;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Agent结算指标
 * Created by yaguang.wang on 2016/9/23.
 */
@Named
public class AgentPerformanceConfigDao extends AlpsStaticMongoDao<AgentPerformanceConfig, String> {

    @Override
    protected void calculateCacheDimensions(AgentPerformanceConfig document, Collection<String> dimensions) {
        dimensions.add(AgentPerformanceConfig.ap_all());
        dimensions.add(AgentPerformanceConfig.ap_u_m(document.getUserId(), document.getSettlementMonth()));
    }

    public List<AgentPerformanceConfig> findAllDictSchool() {
        Criteria criteria = Criteria.where("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    public int deleteAgentPayments(String id) {
        AgentPerformanceConfig agentPerformanceConfig = load(id);
        agentPerformanceConfig.setDisabled(true);
        agentPerformanceConfig = upsert(agentPerformanceConfig);
        if (agentPerformanceConfig != null) {
            Set<String> keys = new HashSet<>();
            keys.add(AgentPerformanceConfig.ap_all());
            keys.add(AgentPerformanceConfig.ap_u_m(agentPerformanceConfig.getUserId(), agentPerformanceConfig.getSettlementMonth()));
            getCache().delete(keys);
        }
        return 1;
    }

    @CacheMethod
    public AgentPerformanceConfig findByUserIdAndMonth(@CacheParameter("ap_u")Long userId, @CacheParameter("ap_m")Integer month){
        Criteria criteria = Criteria.where("userId").is(userId).and("settlementMonth").is(month).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public List<AgentPerformanceConfig> findByMonthList(@CacheParameter("ap_u")Long userId, @UtopiaCacheKey(name = "ap_m", multiple = true) List<Integer> monthList){
        Criteria criteria = Criteria.where("userId").is(userId).and("settlementMonth").in(monthList).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }


}
