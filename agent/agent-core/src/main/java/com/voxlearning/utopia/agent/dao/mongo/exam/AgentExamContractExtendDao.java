package com.voxlearning.utopia.agent.dao.mongo.exam;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContractExtend;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@CacheBean(type = AgentExamContractExtend.class)
public class AgentExamContractExtendDao extends StaticCacheDimensionDocumentMongoDao<AgentExamContractExtend, String> {

    @CacheMethod
    public AgentExamContractExtend loadByContractId(@CacheParameter("cid") Long contractId){
        Criteria criteria = Criteria.where("contractId").is(contractId).and("disabled").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long,AgentExamContractExtend> loadByContractIds(@CacheParameter(value = "cid",multiple = true) Collection<Long> contractIds){
        Criteria criteria = Criteria.where("contractId").in(contractIds).and("disabled").is(false);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(AgentExamContractExtend::getContractId, Function.identity(), (o1, o2) -> o1));
    }

}