package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchers;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 教研员信息固化
 * Created by yaguang.wang on 2016/10/19.
 */
@Named
@CacheBean(type = AgentResearchers.class)
public class AgentResearchersPersistence extends AlpsStaticJdbcDao<AgentResearchers, Long> {
    @Override
    protected void calculateCacheDimensions(AgentResearchers document, Collection<String> dimensions) {
        dimensions.add(AgentResearchers.ck_id(document.getId()));
        dimensions.add(AgentResearchers.ck_u_id(document.getAgentUserId()));
        dimensions.add(AgentResearchers.ck_phone(document.getPhone()));
        dimensions.add(AgentResearchers.ck_province(document.getProvince()));
        dimensions.add(AgentResearchers.ck_name(document.getName()));
//        dimensions.add(AgentResearchers.ck_organization(document.getOrganizationId()));
    }

    @CacheMethod
    public List<AgentResearchers> findAgentResearchersByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("AGENT_USER_ID").is(userId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AgentResearchers> findAgentResearchersByPhone(@CacheParameter("PHONE") String phone) {
        Criteria criteria = Criteria.where("PHONE").is(phone);
        return query(Query.query(criteria)).stream().filter( p -> p.getDisabled() == false).collect(Collectors.toList());
    }

    @CacheMethod
    public List<AgentResearchers> findAgentResearchersByProvinceCode(Integer provinceCode) {
        Criteria criteria = Criteria.where("PROVINCE").is(provinceCode);
        return query(Query.query(criteria)).stream().filter( p -> p.getDisabled() != null && p.getDisabled() == false).collect(Collectors.toList());
    }

    private Query getQuery(Long userId,Collection<Integer> provinceCodes, Collection<Integer> cityCodes,Collection<Integer> countyCodes, String name,  Pageable pageable){
        List<Criteria> list = new LinkedList<>();

        list.add(Criteria.where("disabled").is(false));

        if(CollectionUtils.isNotEmpty(provinceCodes)){
            list.add(Criteria.or(Criteria.where("PROVINCE").in(provinceCodes),Criteria.where("PROVINCE").is(0).and("AGENT_USER_ID").is(userId)));
        }
        if(CollectionUtils.isNotEmpty(cityCodes)){
            list.add(Criteria.or(Criteria.where("CITY").in(cityCodes),Criteria.where("CITY").is(0)));
        }
        if(CollectionUtils.isNotEmpty(countyCodes)){
            list.add(Criteria.or(Criteria.where("COUNTY").in(countyCodes),Criteria.where("COUNTY").is(0)));
        }

        if(StringUtils.isNotBlank(name)){
            list.add(Criteria.where("NAME").like("%" + name + "%"));
        }
        Criteria criteria = Criteria.and(list.toArray(new Criteria[list.size()]));
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        Query query = Query.query(criteria).with(sort);
        if(pageable != null ){
            query =query.with(pageable);
        }
        return query;
    }

    public List<AgentResearchers> findAgentResearchersPage(Long userId,Collection<Integer> provinceCodes, Collection<Integer> cityCodes, Collection<Integer> countyCodes,String name,Pageable pageable) {
        Query query = this.getQuery(userId,provinceCodes, cityCodes,countyCodes, name,pageable);
        return query(query);
    }
    public List<AgentResearchers> findAgentResearchersList(Long userId,Collection<Integer> provinceCodes, Collection<Integer> cityCodes,Collection<Integer> countyCodes,String name) {
        Query query = this.getQuery(userId,provinceCodes, cityCodes,countyCodes, name,null);
        return query(query);
    }
    @CacheMethod
    public List<AgentResearchers> findAgentResearchersByName(@CacheParameter("NAME") String name) {
        Criteria criteria = Criteria.where("disabled").is(false);
        if(StringUtils.isNotBlank(name)){
            criteria.and("NAME").like("%" + name + "%");
        }
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }
    public List<AgentResearchers> findByPage(Pageable pageable ){
        Criteria criteria = new Criteria();
        Query query = Query.query(criteria).with(pageable);
        return query(query);
    }

    @CacheMethod
    public Map<String,List<AgentResearchers>> findAgentResearchersByPhones(@CacheParameter(value = "PHONE" ,multiple = true) Collection<String> phones) {
        Criteria criteria = Criteria.where("PHONE").in(phones);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentResearchers::getPhone));
    }

    public List<AgentResearchers> findAgentResearchersByJob(Integer job) {
        Criteria criteria = Criteria.where("JOB").is(job);
        return query(Query.query(criteria));
    }
//    @CacheMethod
//    public Map<String,List<AgentResearchers>> findListByOrganizationId(@CacheParameter(value = "ORGANIZATION_ID",multiple = true) Collection<String> organizationIds) {
//        if(CollectionUtils.isEmpty(organizationIds)){
//            return Collections.emptyMap();
//        }
//        Criteria criteria = Criteria.where("ORGANIZATION_ID").in(organizationIds);
//        return query(Query.query(criteria)).stream().filter( p -> p.getDisabled() == false).collect(Collectors.groupingBy(AgentResearchers::getOrganizationId));
//    }

    public List<AgentResearchers> findListByRegionCode(Integer provinceCode,Integer cityCode,Integer countyCode){
        if(provinceCode == null || provinceCode <=0){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("province").is(provinceCode);
        if(cityCode != null && cityCode > 0){
            criteria.and("city").is(cityCode);
        }
        if(countyCode != null && countyCode > 0){
            criteria.and("county").is(countyCode);
        }
        return query(Query.query(criteria));
    }
}
