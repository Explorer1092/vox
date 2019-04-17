package com.voxlearning.utopia.agent.dao.mongo.workload;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.workload.AgentRecordWorkload;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentWorkRecordType;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentRecordWorkloadDao
 *
 * @author song.wang
 * @date 2018/6/13
 */
@Named
@CacheBean(type = AgentRecordWorkload.class)
public class AgentRecordWorkloadDao extends StaticCacheDimensionDocumentMongoDao<AgentRecordWorkload, String> {

    @CacheMethod
    public AgentRecordWorkload loadByWorkRecordIdAndType(@CacheParameter(value = "wrId") String workRecordId, @CacheParameter(value = "wrType") AgentWorkRecordType workRecordType){
        Criteria criteria = Criteria.where("workRecordId").is(workRecordId);
        criteria.and("workRecordType").is(workRecordType);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<String,AgentRecordWorkload> loadByWorkRecordIdsAndType(@CacheParameter(value = "wrId",multiple = true) Collection<String> workRecordIds,@CacheParameter(value = "wrType") AgentWorkRecordType workRecordType){
        Criteria criteria = Criteria.where("workRecordId").in(workRecordIds);
        criteria.and("workRecordType").is(workRecordType);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(AgentRecordWorkload::getWorkRecordId, Function.identity(), (o1, o2) -> o1));
    }
}
