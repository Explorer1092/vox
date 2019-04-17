package com.voxlearning.utopia.agent.dao.mongo.parent;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.parent.AgentSchoolNewRegisterParent;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentSchoolNewRegisterParent.class)
public class AgentNewRegisterParentDao extends StaticCacheDimensionDocumentMongoDao<AgentSchoolNewRegisterParent, String> {

    @CacheMethod
    public Map<Long,List<AgentSchoolNewRegisterParent>> loadBySchoolIds(@CacheParameter(value = "sid",multiple = true) Collection<Long> schoolIds){
        if (CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("schoolId").in(schoolIds);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentSchoolNewRegisterParent::getSchoolId));
    }

}