package com.voxlearning.utopia.agent.dao.mongo.material;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBalanceChangeRecord;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBudget;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chunlin.yu
 * @create 2018-02-06 18:45
 **/
@Named
@CacheBean(type = AgentMaterialBalanceChangeRecord.class)
public class AgentMaterialBalanceChangeRecordDao extends StaticCacheDimensionDocumentMongoDao<AgentMaterialBalanceChangeRecord, String> {

    public List<AgentMaterialBalanceChangeRecord> getByBudgetId(String budgetId,Integer recordType){
        if(StringUtils.isBlank(budgetId)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("agentMaterialBudgetId").is(budgetId);
        if (recordType != null){
            criteria.and("recordType").is(recordType);
        }
        Query query = Query.query(criteria);
        return query(query);
    }
}
