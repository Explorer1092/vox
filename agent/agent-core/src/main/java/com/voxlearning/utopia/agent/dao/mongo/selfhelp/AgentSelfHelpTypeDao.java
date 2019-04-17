package com.voxlearning.utopia.agent.dao.mongo.selfhelp;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.selfhelp.AgentSelfHelpType;

import javax.inject.Named;
import java.util.List;

/**
 * AgentSelfHelpTypeDao
 *
 * @author song.wang
 * @date 2018/6/7
 */
@Named
@CacheBean(type = AgentSelfHelpType.class)
public class AgentSelfHelpTypeDao extends StaticCacheDimensionDocumentMongoDao<AgentSelfHelpType, String> {

    @CacheMethod(key="all")
    public List<AgentSelfHelpType> findAllAvailable(){
        Criteria criteria = Criteria.where("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }
}
