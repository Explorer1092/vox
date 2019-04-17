package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentSchoolBudget;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chunlin.yu
 * @create 2017-08-08 15:30
 **/
@Named
@CacheBean(type = AgentSchoolBudget.class)
public class AgentSchoolBudgetDao extends StaticCacheDimensionDocumentMongoDao<AgentSchoolBudget, String> {

    @CacheMethod
    public List<AgentSchoolBudget> loadBySchoolId(@CacheParameter("sid") Long schoolId){
        if (null == schoolId) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("schoolId").is(schoolId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public Map<Long, List<AgentSchoolBudget>> loadBySchoolIds(@CacheParameter(value = "sid", multiple = true) Collection<Long> schoolIds) {
        if (!CollectionUtils.isNotEmpty(schoolIds)) {
            return new HashMap<>();
        }
        Criteria criteria = Criteria.where("schoolId").in(schoolIds).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.groupingBy(AgentSchoolBudget::getSchoolId,Collectors.toList()));
    }

    @CacheMethod
    public List<AgentSchoolBudget> loadByMonth(@CacheParameter("m") Integer month){
        if (null == month) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("month").is(month).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public Map<Long, AgentSchoolBudget> loadBySchoolsAndMonth(@CacheParameter(value = "sid", multiple = true) Collection<Long> schoolIds, @CacheParameter("m") Integer month){
        Criteria criteria = Criteria.where("schoolId").in(schoolIds).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().filter(p -> Objects.equals(p.getMonth(), month)).collect(Collectors.toMap(AgentSchoolBudget::getSchoolId, Function.identity(), (o1, o2) -> o1));
    }

}
