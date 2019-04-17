package com.voxlearning.utopia.agent.dao.mongo.selfhelp;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.selfhelp.AgentSelfHelp;

import javax.inject.Named;
import java.util.List;

/**
 * AgentSelfHelpDao
 *
 * @author song.wang
 * @date 2018/6/7
 */
@Named
@CacheBean(type = AgentSelfHelp.class)
public class AgentSelfHelpDao extends StaticCacheDimensionDocumentMongoDao<AgentSelfHelp, String> {

    @CacheMethod
    public List<AgentSelfHelp> findByType(@CacheParameter("type")String typeId){
        Criteria criteria = Criteria.where("typeId").is(typeId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod(key="all")
    public List<AgentSelfHelp> findAllAvailable(){
        Criteria criteria = Criteria.where("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

}
