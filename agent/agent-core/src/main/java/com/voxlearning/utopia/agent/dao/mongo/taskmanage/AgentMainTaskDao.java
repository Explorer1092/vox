package com.voxlearning.utopia.agent.dao.mongo.taskmanage;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.taskmanage.AgentMainTask;

import javax.inject.Named;
import java.util.Date;
import java.util.List;


@Named
@CacheBean(type = AgentMainTask.class)
public class AgentMainTaskDao extends StaticCacheDimensionDocumentMongoDao<AgentMainTask, String> {

    //查询过期时间小于30天的所有任务
    public List<AgentMainTask> findTaskMainList() {
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("endTime").gte(DateUtils.calculateDateDay(new Date(),-30));
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<AgentMainTask> loadByCreateTime(Date createTime){
        Criteria criteria = Criteria.where("disabled").is(false);
        if (createTime != null){
            criteria.and("createTime").gte(createTime);
        }
        return query(Query.query(criteria));
    }

    public List<AgentMainTask> loadByTitle(String title){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("title").is(title);
        return query(Query.query(criteria));
    }

    /**
     * 查询未结束的任务列表
     * @return
     */
    public List<AgentMainTask> loadUnEndTaskList(){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("endTime").gte(new Date());
        return query(Query.query(criteria));
    }

}