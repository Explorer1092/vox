package com.voxlearning.utopia.agent.persist.exam;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContractPayback;

import javax.inject.Named;
import java.util.*;

/**
 * AgentLargeExamContractPayback的dao层
 *
 * @author deliang.che
 * @data 2018-05-03
 **/

@Named
@CacheBean(type = AgentExamContractPayback.class)
public class AgentExamContractPaybackPersistence extends AgentExamBasePersistence<AgentExamContractPayback, Long> {

    public List<AgentExamContractPayback> loadByContractId(Long contractId) {
        if (null == contractId) {
            return Collections.emptyList();
        }
        Criteria criteria = generateCriteriaWithDisabledField(false).and("CONTRACT_ID").is(contractId);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<AgentExamContractPayback> searchContractPayback(Long contractId,Integer period,Date beginDate, Date endDate) {
        Criteria criteria = generateCriteriaWithDisabledField(false);
        if (null != contractId){
            criteria.and("CONTRACT_ID").is(contractId);
        }
        if (null != period){
            criteria.and("PERIOD").lt(period);
        }
        if (null != beginDate || null != endDate) {
            criteria.and("PAYBACK_DATE");
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

    public Long deleteByContractId(Long contractId){
        Update update = Update.update("disabled", true);
        Criteria criteria = generateCriteriaWithDisabledField(false).and("CONTRACT_ID").is(contractId);
        return $update(update,criteria);
    }

    @Override
    protected void calculateCacheDimensions(AgentExamContractPayback document, Collection<String> dimensions) {

    }
}
