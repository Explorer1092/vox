package com.voxlearning.utopia.agent.dao.mongo.material;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBudget;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chunlin.yu
 * @create 2018-02-06 14:26
 **/
@Named
@CacheBean(type = AgentMaterialBudget.class)
public class AgentMaterialBudgetDao extends StaticCacheDimensionDocumentMongoDao<AgentMaterialBudget, String> {

    public void insert(AgentMaterialBudget agentMaterialBudget) {
        if (null == agentMaterialBudget.getDisabled()){
            agentMaterialBudget.setDisabled(false);
        }
        super.insert(agentMaterialBudget);
    }

    public AgentMaterialBudget replace(AgentMaterialBudget agentMaterialBudget) {
        if (null == agentMaterialBudget.getDisabled()){
            agentMaterialBudget.setDisabled(false);
        }
        super.replace(agentMaterialBudget);
        return agentMaterialBudget;
    }

    /**
     * 物料预算按照UserId查询
     * @return
     */
    public List<AgentMaterialBudget> getMaterialBudgetsByUserId(Collection<Long> userIds){
        if (CollectionUtils.isEmpty(userIds)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("budgetType").is(2).and("userId").in(userIds).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    /**
     * 查询部门城市预算
     * @param groupId
     * @param regionCode
     * @return
     */
    public List<AgentMaterialBudget> getCityBudgets(long groupId,int regionCode){
        Criteria criteria =  Criteria.where("groupId").is(groupId).and("regionCode").is(regionCode).and("budgetType").is(1).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<AgentMaterialBudget> getCityBudgets(long groupId,int regionCode,int startMonth){
        Criteria criteria =  Criteria.where("groupId").is(groupId).and("regionCode").is(regionCode).and("month").gt(startMonth).and("budgetType").is(1).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<AgentMaterialBudget> getCityBudgets(Collection<Long> groupIds,int startMonth){
        if (CollectionUtils.isEmpty(groupIds)){
            return Collections.emptyList();
        }
        Criteria criteria =  Criteria.where("groupId").in(groupIds).and("month").gt(startMonth).and("budgetType").is(1).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<AgentMaterialBudget> searchCityBudgets(String groupName,String cityName,int beginMonth,int endMonth){
        Criteria criteria =  Criteria.where("budgetType").is(1).and("disabled").is(false);
        if (StringUtils.isNotEmpty(groupName)){
            criteria.and("groupName").is(groupName);
        }
        if (StringUtils.isNotEmpty(cityName)){
            criteria.and("regionName").is(cityName);
        }
        if (beginMonth > 0 || endMonth > 0){
            criteria = criteria.and("month");
            if (beginMonth > 0){
                criteria.gte(beginMonth);
            }
            if (endMonth > 0){
                criteria.lte(endMonth);
            }
        }
        Query query = Query.query(criteria);
        return query(query);
    }

    /**
     * 物料预算
     * @return
     */
    public Map<Long,List<AgentMaterialBudget>> getAllMaterialBudgets(){
        Criteria criteria = Criteria.where("budgetType").is(2).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.groupingBy(AgentMaterialBudget::getUserId,Collectors.toList()));
    }


    /**
     * 城市预算
     * @return
     */
    public Map<Integer,List<AgentMaterialBudget>> getAllCityBudgets(){
        Criteria criteria = Criteria.where("budgetType").is(1).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.groupingBy(AgentMaterialBudget::getRegionCode,Collectors.toList()));
    }

}
