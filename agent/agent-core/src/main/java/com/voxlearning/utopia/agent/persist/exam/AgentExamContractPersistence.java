package com.voxlearning.utopia.agent.persist.exam;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.utopia.agent.constants.AgentLargeExamContractType;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContract;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * AgentLargeExamContract的dao层
 *
 * @author chunlin.yu
 * @create 2018-03-13 13:26
 **/

@Named
@CacheBean(type = AgentExamContract.class)
public class AgentExamContractPersistence extends AgentExamBasePersistence<AgentExamContract, Long> {


    public AgentExamContract loadBySchoolId(Long schoolId) {
        if (null == schoolId) {
            return null;
        }
        Criteria criteria = generateCriteriaWithDisabledField(false).and("SCHOOL_ID").is(schoolId);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    public List<AgentExamContract> loadBySchoolIds(List<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        Criteria criteria = generateCriteriaWithDisabledField(false).and("SCHOOL_ID").in(schoolIds);
        Query query = Query.query(criteria);
        return query(query);
    }


    @Override
    protected void calculateCacheDimensions(AgentExamContract document, Collection<String> dimensions) {

    }

    public List<AgentExamContract> searchContract(Long id, Long schoolId, Long contractorId, AgentLargeExamContractType contractType, Date beginDate, Date endDate) {
        Criteria criteria = generateCriteriaWithDisabledField(false);
        if (id != null) {
            criteria.and("ID").is(id);
        }
        if (schoolId != null) {
            criteria.and("SCHOOL_ID").is(schoolId);
        }
        if (null != contractorId) {
            criteria.and("CONTRACTOR_ID").is(contractorId);
        }
        if (null != contractType) {
            criteria.and("CONTRACT_TYPE").is(contractType);
        }
        if (null != beginDate || null != endDate) {
            criteria.and("CONTRACT_DATE");
            if (beginDate != null) {
                criteria.gte(beginDate);
            }
            if (endDate != null) {
                criteria.lte(endDate);
            }
        }
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<AgentExamContract> loadByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        Criteria criteria = generateCriteriaWithDisabledField(false).and("ID").in(ids);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<AgentExamContract> loadInServiceContract(Date date){
        Criteria criteria = Criteria.where("DISABLED").is(false);
        criteria.and("BEGIN_DATE").lte(date);
        criteria.and("END_DATE").gt(date);
        Query query = Query.query(criteria);
        return query(query);
    }
}
