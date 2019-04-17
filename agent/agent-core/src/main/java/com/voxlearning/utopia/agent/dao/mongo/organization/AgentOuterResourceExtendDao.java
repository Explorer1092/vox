package com.voxlearning.utopia.agent.dao.mongo.organization;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOrganization;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResourceExtend;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentOuterResourceExtend.class)
public class AgentOuterResourceExtendDao extends StaticCacheDimensionDocumentMongoDao<AgentOuterResourceExtend, String> {
    @CacheMethod
    public Map<Long,List<AgentOuterResourceExtend>> findListByOrganizationIds(@CacheParameter(value = "ORGANIZATION_ID",multiple = true) Collection<Long> organizationIds) {
        if(CollectionUtils.isEmpty(organizationIds)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("organizationId").in(organizationIds);
        return query(Query.query(criteria)).stream().filter(p -> p.getDisabled() == false).collect(Collectors.groupingBy(AgentOuterResourceExtend::getOrganizationId));
    }

    @CacheMethod
    public Map<Long,AgentOuterResourceExtend> findListByOuterResourceIds(@CacheParameter(value = "resourceId",multiple = true) Collection<Long> resourceIds) {
        if(CollectionUtils.isEmpty(resourceIds)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("resourceId").in(resourceIds);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(AgentOuterResourceExtend::getResourceId, Function.identity(),(o1, o2) -> o1));
    }

    @CacheMethod
    public AgentOuterResourceExtend loadByResourceId(@CacheParameter(value = "resourceId")Long resourceId){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("resourceId").is(resourceId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
//    @CacheMethod
//    public Map<Long,List<AgentOuterResourceExtend>> findListBySchoolIds(@CacheParameter(value = "schoolId",multiple = true) Collection<Long> schoolIds) {
//        if(CollectionUtils.isEmpty(schoolIds)){
//            return Collections.emptyMap();
//        }
//        Criteria criteria = Criteria.where("schoolId").in(schoolIds);
//        return query(Query.query(criteria)).stream().filter(p -> p.getDisabled() == false).collect(Collectors.groupingBy(AgentOuterResourceExtend::getSchoolId));
//    }

}