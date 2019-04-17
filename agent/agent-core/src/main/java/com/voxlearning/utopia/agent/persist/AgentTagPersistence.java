package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.constants.AgentTagSubType;
import com.voxlearning.utopia.agent.constants.AgentTagType;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTag;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * 标签Persistence
 *
 * @author deliang.che
 * @since  2019/3/20
 */
@Named
public class AgentTagPersistence extends AlpsStaticJdbcDao<AgentTag, Long> {
    @Override
    protected void calculateCacheDimensions(AgentTag source, Collection<String> dimensions) {

    }

    public List<AgentTag> loadAll(){
        Criteria criteria = Criteria.where("disabled").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AgentTag> loadByName(@CacheParameter(value = "name") String name){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("name").is(name);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AgentTag> loadByType(@CacheParameter(value = "tagType") AgentTagType tagType){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("TAG_TYPE").is(tagType);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AgentTag> loadByTypeAndSubType(@CacheParameter(value = "tagType") AgentTagType tagType ,@CacheParameter(value = "tagSubType") AgentTagSubType tagSubType){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("TAG_TYPE").is(tagType);
        criteria.and("TAG_SUB_TYPE").is(tagSubType);
        return query(Query.query(criteria));
    }
}
