package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.agent.constants.AgentUserOperationType;
import com.voxlearning.utopia.agent.persist.entity.AgentUserOperationRecord;

import javax.inject.Named;
import java.util.List;

/**
 * AgentUserOperationRecordDao
 *
 * @author song.wang
 * @date 2018/1/11
 */
@Named
@CacheBean(type = AgentUserOperationRecord.class)
public class AgentUserOperationRecordDao extends StaticCacheDimensionDocumentMongoDao<AgentUserOperationRecord, String> {

    @CacheMethod
    public List<AgentUserOperationRecord> findByTypeAndTeacherId(@CacheParameter("type") AgentUserOperationType operationType, @CacheParameter("tid")Integer teacherId){
        Criteria criteria = Criteria.where("operationType").is(operationType);
        criteria.and("createTime").gte(DateUtils.addMonths(DayRange.current().getStartDate(), -6)); // 查询最近6个月的数据
        criteria.and("teacherId").is(teacherId);
        Query query = new Query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<AgentUserOperationRecord> findByType(@CacheParameter("type") AgentUserOperationType operationType){
        Criteria criteria = Criteria.where("operationType").is(operationType);
        criteria.and("createTime").gte(DateUtils.addMonths(DayRange.current().getStartDate(), -6)); // 查询最近6个月的数据
        Query query = new Query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<AgentUserOperationRecord> findByDataId(@CacheParameter("did")String dataId){
        Criteria criteria = Criteria.where("dataId").is(dataId);
        criteria.and("createTime").gte(DateUtils.addMonths(DayRange.current().getStartDate(), -6)); // 查询最近6个月的数据
        Query query = new Query(criteria);
        return query(query);
    }
}
