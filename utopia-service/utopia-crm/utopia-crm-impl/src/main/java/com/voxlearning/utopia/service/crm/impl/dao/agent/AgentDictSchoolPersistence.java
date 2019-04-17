package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;


import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentDictSchoolPersistence
 *
 * @author song.wang
 * @date 2016/6/24
 */
@Named
@CacheBean(type = AgentDictSchool.class)
public class AgentDictSchoolPersistence extends AlpsStaticJdbcDao<AgentDictSchool, Long> {

    @Override
    protected void calculateCacheDimensions(AgentDictSchool source, Collection<String> dimensions) {
//        dimensions.add(AgentDictSchool.ck_all());
        dimensions.add(AgentDictSchool.ck_sId(source.getSchoolId()));
        dimensions.add(AgentDictSchool.ck_county_code(source.getCountyCode()));
    }


//    @CacheMethod(key = "ALL")
    @Deprecated
    public List<AgentDictSchool> findAllDictSchool() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }


    @CacheMethod
    public AgentDictSchool findBySchoolId(@CacheParameter("s_id")Long schoolId) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId);
        criteria.and("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }


    @CacheMethod
    public Map<Long, AgentDictSchool> findBySchoolIds(@CacheParameter(value = "s_id", multiple = true)Collection<Long> schoolIds) {
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("SCHOOL_ID").in(schoolIds);
        criteria.and("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.toMap(AgentDictSchool::getSchoolId, Function.identity(), (o1, o2) -> o1));
    }

    @CacheMethod
    public List<AgentDictSchool> findByCountyCode(@CacheParameter("county_code") Integer countyCode){
        Criteria criteria = Criteria.where("COUNTY_CODE").is(countyCode);
        criteria.and("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public Map<Integer, List<AgentDictSchool>> findByCountyCodes(@CacheParameter(value = "county_code", multiple = true) Collection<Integer> countyCodes){
        if(CollectionUtils.isEmpty(countyCodes)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("COUNTY_CODE").in(countyCodes);
        criteria.and("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.groupingBy(AgentDictSchool::getCountyCode, Collectors.toList()));
    }

    public void unsetField(String filedName,Long documentId){
        Update update = new Update().set("UPDATE_DATETIME", new Date()).unset(filedName);
        Criteria criteria = Criteria.where("ID").is(documentId);
        List<AgentDictSchool> dictSchoolList = query(Query.query(criteria));
        if (CollectionUtils.isNotEmpty(dictSchoolList)) {
            int count = (int) $update(update, criteria);
            if (count > 0) {
                evictDocumentCache(dictSchoolList);
            }
        }
    }


    public int deleteDictDimSchool(Long id) {

        Update update = Update.update("DISABLED", true).set("UPDATE_DATETIME", new Date());
        Criteria criteria = Criteria.where("ID").is(id);
        List<AgentDictSchool> dictSchoolList = query(Query.query(criteria));
        if (CollectionUtils.isEmpty(dictSchoolList)) {
            return 0 ;
        }
        int count = (int) $update(update, criteria);
        if (count > 0) {
            evictDocumentCache(dictSchoolList);
        }
        return count;
    }

}
