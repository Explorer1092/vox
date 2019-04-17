package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceGoal;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceServiceRecord;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.*;

/**
 * Agent业绩目标服务记录
 *
 * @author chunlin.yu
 * @create 2017-10-30 19:40
 **/
@Named
@CacheBean(type = AgentPerformanceServiceRecord.class)
public class AgentPerformanceServiceRecordDao  extends StaticCacheDimensionDocumentMongoDao<AgentPerformanceServiceRecord, String> {

    @CacheMethod
    public List<AgentPerformanceServiceRecord> load(@CacheParameter("m") Integer month, @CacheParameter("t") Long targetId, @CacheParameter("a") AgentPerformanceGoalType agentPerformanceGoalType){
        if (null == month || null == targetId || null == agentPerformanceGoalType) {
            return emptyList();
        }
        Criteria criteria = Criteria.where("month").is(month).and("targetId").is(targetId).and("agentPerformanceGoalType").is(agentPerformanceGoalType).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }
}
