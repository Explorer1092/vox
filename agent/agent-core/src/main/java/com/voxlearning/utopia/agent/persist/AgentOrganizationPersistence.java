package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOrganization;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRegionRank;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 机构Persistence
 *
 * @author deliang.che
 * @since  2019/1/11
 */
@Named
public class AgentOrganizationPersistence extends AlpsStaticJdbcDao<AgentOrganization, Long> {
    @Override
    protected void calculateCacheDimensions(AgentOrganization source, Collection<String> dimensions) {

    }

    public List<AgentOrganization> loadByCityCode(Integer cityCode){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("CITY_CODE").is(cityCode);
        criteria.and("ORG_TYPE").is(1);
        return query(Query.query(criteria));
    }

    public List<AgentOrganization> loadByProvinceCode(Integer provinceCode){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("PROVINCE_CODE").is(provinceCode);
        criteria.and("ORG_TYPE").is(1);
        return query(Query.query(criteria));
    }

    public List<AgentOrganization> loadByRegionRank(AgentRegionRank regionRank){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("REGION_RANK").is(regionRank);
        return query(Query.query(criteria));
    }

    public List<AgentOrganization> loadByRegionCodes(Collection<Integer> provinceCodes,Collection<Integer> cityCodes,Collection<Integer> countyCodes){
        if(CollectionUtils.isEmpty(provinceCodes) && CollectionUtils.isEmpty(cityCodes) && CollectionUtils.isEmpty(countyCodes)){
            return Collections.emptyList();
        }
        List<Criteria> list = new LinkedList<>();

        list.add(Criteria.where("disabled").is(false));

        if(CollectionUtils.isNotEmpty(provinceCodes)){
            list.add(Criteria.or(Criteria.where("PROVINCE_CODE").in(provinceCodes),Criteria.where("PROVINCE_CODE").is(0)));
        }
        if(CollectionUtils.isNotEmpty(cityCodes)){
            list.add(Criteria.or(Criteria.where("CITY_CODE").in(cityCodes),Criteria.where("CITY_CODE").is(0)));
        }
        if(CollectionUtils.isNotEmpty(countyCodes)){
            list.add(Criteria.or(Criteria.where("COUNTY_CODE").in(countyCodes),Criteria.where("COUNTY_CODE").is(0)));
        }

        Criteria criteria = Criteria.and(list.toArray(new Criteria[list.size()]));
        return query(Query.query(criteria));
    }
    @CacheMethod
    public AgentOrganization loadBySchoolIdAndOrgType(@CacheParameter(value = "sid")Long schoolId){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("SCHOOL_ID").is(schoolId).and("ORG_TYPE").is(2);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }


    public List<AgentOrganization> loadByName(String orgName){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("name").is(orgName);
        return query(Query.query(criteria));
    }
}
