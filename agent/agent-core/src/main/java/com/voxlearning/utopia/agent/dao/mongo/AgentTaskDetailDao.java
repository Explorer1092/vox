package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentTask;
import com.voxlearning.utopia.entity.agent.AgentTaskDetail;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/5/10.
 */
@Named
@CacheBean(type = AgentTaskDetail.class)
public class AgentTaskDetailDao extends AlpsStaticMongoDao<AgentTaskDetail, String> {
    @Override
    protected void calculateCacheDimensions(AgentTaskDetail source, Collection<String> dimensions) {
        dimensions.add(AgentTaskDetail.ck_taskId(source.getTaskId()));
        dimensions.add(AgentTaskDetail.ck_uid(source.getExecutorId()));
        dimensions.add(AgentTaskDetail.ck_tid(source.getTeacherId()));
    }

    @CacheMethod
    public List<AgentTaskDetail> findByTaskId(@CacheParameter("taskId") String taskId) {
        Criteria criteria = Criteria.where("taskId").is(taskId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<AgentTaskDetail> findByUserId(@CacheParameter("userId") Long userId) {
        Criteria criteria = Criteria.where("executorId").is(userId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<AgentTaskDetail> findByTeacherId(@CacheParameter("teacherId") Long teacherId){
        Criteria criteria = Criteria.where("teacherId").is(teacherId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }


    public List<AgentTaskDetail> findByCreateTime(Date startDate, Date endDate){
        if(startDate == null && endDate == null){
            return Collections.emptyList();
        }
        Criteria criteria = new Criteria();
        if(startDate != null && endDate != null){
            criteria.and("createTime").gte(startDate).lt(endDate);
        }else if(startDate != null){
            criteria.and("createTime").gte(startDate);
        }else {
            criteria.and("createTime").lt(endDate);
        }
        criteria.and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }



}
