package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResource;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentOuterResource.class)
public class AgentOuterResourcePersistence extends AlpsStaticJdbcDao<AgentOuterResource, Long> {

    @Override
    protected void calculateCacheDimensions(AgentOuterResource document, Collection<String> dimensions) {
        dimensions.add(AgentOuterResource.ck_id(document.getId()));
    }



    public List<AgentOuterResource> findListByIdsAndName(Collection<Long> resourceIds,String name) {
        if(CollectionUtils.isEmpty(resourceIds) && StringUtils.isBlank(name)){
            return Collections.emptyList();
        }
        Criteria criteria =Criteria.where("disabled").is(false);
        if(StringUtils.isNotBlank(name)){
            criteria.and("NAME").like("%" + name + "%");
        }
        if(CollectionUtils.isNotEmpty(resourceIds)){
            criteria.and("id").in(resourceIds);
        }
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public Map<Long,AgentOuterResource> findListByIds(@CacheParameter(value = "ID",multiple = true) Collection<Long> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return Collections.emptyMap();
        }
        Criteria criteria =Criteria.where("disabled").is(false);
        criteria.and("id").in(ids);
        Query query = Query.query(criteria);
        return Optional.ofNullable(query(query)).orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.toMap(AgentOuterResource::getId, Function.identity(), (o1, o2) -> o2));
    }

}