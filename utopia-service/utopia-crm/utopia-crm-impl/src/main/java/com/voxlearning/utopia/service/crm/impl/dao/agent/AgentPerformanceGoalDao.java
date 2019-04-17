package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceGoal;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * Agent 业绩目标Dao
 *
 * @author chunlin.yu
 * @create 2017-10-26 14:25
 **/
@Named
@CacheBean(type = AgentPerformanceGoal.class)
public class AgentPerformanceGoalDao  extends StaticCacheDimensionDocumentMongoDao<AgentPerformanceGoal, String> {

    @CacheMethod
    public List<AgentPerformanceGoal> loadByMonth(@CacheParameter("m") Integer month){
        if (null == month) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("month").is(month).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<AgentPerformanceGoal> loadByMonth(@CacheParameter("m") Integer month,@CacheParameter("c") boolean confirm){
        if (null == month) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("month").is(month).and("confirm").is(confirm).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    /**
     * 根据开始月份查询大于等于的数据
     * @param beginMonth
     * @return
     */
    public List<AgentPerformanceGoal> loadByBeginMonth(Integer beginMonth){
        if (null == beginMonth) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("month").gte(beginMonth).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }


}
