package com.voxlearning.utopia.agent.dao.mongo.messagecenter;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.messagecenter.AgentGroupRoleAuthority;

import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentGroupRoleAuthority.class)
public class AgentGroupRoleAuthorityDao extends StaticCacheDimensionDocumentMongoDao<AgentGroupRoleAuthority, String> {
    @CacheMethod
    public List<AgentGroupRoleAuthority> findBySourceId(@CacheParameter("sourceId")String sourceId){
        Criteria criteria = Criteria.where("sourceId").is(sourceId);
        Query query = Query.query(criteria);
        return query(query).stream().filter(p -> p.getDisabled() == false).collect(Collectors.toList());
    }
}
