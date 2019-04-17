package com.voxlearning.utopia.agent.dao.mongo.authority;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.authority.AgentRecordAuthority;

import javax.inject.Named;

@Named
@CacheBean(type = AgentRecordAuthority.class)
public class AgentRecordAuthorityDao extends StaticCacheDimensionDocumentMongoDao<AgentRecordAuthority, String> {

    @CacheMethod
    public AgentRecordAuthority loadByRidAndType(@CacheParameter("rid") String recordId, @CacheParameter("t")Integer recordType){
        if(StringUtils.isBlank(recordId) || recordType == null){
            return null;
        }
        Criteria criteria = Criteria.where("recordId").is(recordId).and("recordType").is(recordType).and("disabled").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

}
