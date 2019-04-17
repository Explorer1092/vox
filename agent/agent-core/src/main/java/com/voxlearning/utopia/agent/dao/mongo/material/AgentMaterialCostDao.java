package com.voxlearning.utopia.agent.dao.mongo.material;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialCost;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author deliang.che
 * @since 2018/7/17
 **/
@Named
@CacheBean(type = AgentMaterialCost.class)
public class AgentMaterialCostDao extends StaticCacheDimensionDocumentMongoDao<AgentMaterialCost, String> {

    public List<AgentMaterialCost> getGroupMaterialCost(){
        Criteria criteria = Criteria.where("materialType").is(1).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    public List<AgentMaterialCost> getGroupMaterialCostByGroupId(Long groupId){
        Criteria criteria = Criteria.where("materialType").is(1).and("disabled").is(false);
        criteria.and("groupId").is(groupId);
        return query(Query.query(criteria));
    }

    public List<AgentMaterialCost> getUserMaterialCost(){
        Criteria criteria = Criteria.where("materialType").is(2).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    public List<AgentMaterialCost> getUserMaterialCostByGroupId(Long groupId){
        Criteria criteria = Criteria.where("materialType").is(2).and("disabled").is(false);
        criteria.and("groupId").is(groupId);
        return query(Query.query(criteria));
    }

    public Map<Long,List<AgentMaterialCost>> getUserMaterialCostByGroupIds(Collection<Long> groupIds){
        Criteria criteria = Criteria.where("materialType").is(2).and("disabled").is(false);
        criteria.and("groupId").in(groupIds);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentMaterialCost::getGroupId,Collectors.toList()));
    }

    public List<AgentMaterialCost> getUserMaterialCostByUserId(Long userId){
        Criteria criteria = Criteria.where("materialType").is(2).and("disabled").is(false);
        criteria.and("userId").is(userId);
        return query(Query.query(criteria));
    }

}
