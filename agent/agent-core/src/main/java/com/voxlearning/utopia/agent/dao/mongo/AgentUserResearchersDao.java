/**
 * Author:   xianlong.zhang
 * Date:     2018/8/3 12:17
 * Description: 专员教研员关系表Dao
 * History:
 */
package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentUserResearchers;

import javax.inject.Named;
import java.util.List;

@Named
@CacheBean(type = AgentUserResearchers.class)
public class AgentUserResearchersDao  extends StaticCacheDimensionDocumentMongoDao<AgentUserResearchers, String> {
    @CacheMethod
    public List<AgentUserResearchers> findUserResearchersByAgentUserId(@CacheParameter("userId")Long agentUserId){
        Criteria criteria = Criteria.where("userId").is(agentUserId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<AgentUserResearchers> findByUserIdAndResearchersId(@CacheParameter("userId") Long userId, @CacheParameter("researchersId")Long researchersId){
        Criteria criteria = Criteria.where("userId").is(userId);
        criteria.and("researchersId").is(researchersId);
        Query query = new Query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<AgentUserResearchers> findUserResearchersByResourceId(@CacheParameter("researchersId")Long researchersId){
        Criteria criteria = Criteria.where("researchersId").is(researchersId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

}
