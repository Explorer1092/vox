package com.voxlearning.utopia.agent.dao.mongo.trainingcenter;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentTitleColumn;

import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
@CacheBean(type = AgentTitleColumn.class)
public class AgentTitleColumnDao extends StaticCacheDimensionDocumentMongoDao<AgentTitleColumn, String> {

    @CacheMethod
    public AgentTitleColumn findById(String id){
        if(StringUtils.isBlank(id)){
            return null;
        }
        AgentTitleColumn agentTitleColumn = load(id);
        if(agentTitleColumn == null || SafeConverter.toBoolean(agentTitleColumn.getDisabled())){
            return null;
        }
        return agentTitleColumn;
    }

    @CacheMethod
    public List<AgentTitleColumn> findByParentId(@CacheParameter("pid") String parentId) {
        Criteria criteria = Criteria.where("parentId").is(parentId);
        Query query = Query.query(criteria);
        return query(query).stream().filter(p -> p.getDisabled() == false).collect(Collectors.toList());
    }

    @CacheMethod(key = "all")
    public List<AgentTitleColumn> findAll(){
        return query().stream().filter(p -> p.getDisabled() == false).collect(Collectors.toList());
    }

}
