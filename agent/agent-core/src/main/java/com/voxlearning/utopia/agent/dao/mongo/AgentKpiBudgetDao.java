package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentKpiBudget;

import javax.inject.Named;
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2018/2/11
 */
@Named
@CacheBean(type = AgentKpiBudget.class)
public class AgentKpiBudgetDao extends StaticCacheDimensionDocumentMongoDao<AgentKpiBudget, String> {

    @CacheMethod
    public List<AgentKpiBudget> loadByMonth(@CacheParameter("month")Integer month){
        Criteria criteria = Criteria.where("month").is(month).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }
}
