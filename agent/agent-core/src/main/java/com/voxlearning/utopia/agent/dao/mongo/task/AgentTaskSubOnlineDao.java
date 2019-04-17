package com.voxlearning.utopia.agent.dao.mongo.task;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.task.AgentTaskSubOnline;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentTaskSubOnline.class)
public class AgentTaskSubOnlineDao extends StaticCacheDimensionDocumentMongoDao<AgentTaskSubOnline, String> {
    public void deleteByMainTaskId(String mainTaskId){

        Update update = Update.update("disabled", true);
        Criteria criteria = Criteria.where("mainTaskId").is(mainTaskId);
        executeUpdateMany(createMongoConnection(), criteria, update);
        // 清除缓存
        Query query = Query.query(criteria);
        List<AgentTaskSubOnline> list = query(query);
        evictDocumentCache(list);
    }

    public void updateOperator(Collection<String> ids, Long operatorId, String operatorName){
        Update update = Update.update("operatorId", operatorId);
        update.set("operatorName",operatorName);
        Criteria criteria = Criteria.where("_id").in(ids);
        executeUpdateMany(createMongoConnection(), criteria, update);
        // 清除缓存
        Query query = Query.query(criteria);
        List<AgentTaskSubOnline> list = query(query);
        evictDocumentCache(list);
    }

    @CacheMethod
    public List<AgentTaskSubOnline> loadByMainTaskId(@CacheParameter(value = "mid") String mainTaskId){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("mainTaskId").is(mainTaskId);
        return query(Query.query(criteria));
    }

    public void updateIsHomework(Collection<String> mainTaskIds,Long teacherId){
        Update update = Update.update("isHomework", true);
        update.set("homeworkTime",new Date());
        Criteria criteria = Criteria.where("mainTaskId").in(mainTaskIds);
        criteria.and("teacherId").is(teacherId);
        criteria.and("isHomework").is(false);
        criteria.and("disabled").is(false);

        Query query = Query.query(criteria);
        List<AgentTaskSubOnline> list = query(query);

        executeUpdateMany(createMongoConnection(), criteria, update);
        // 清除缓存
        evictDocumentCache(list);
    }

    @CacheMethod
    public Map<String,List<AgentTaskSubOnline>> loadByMainTaskIds(@CacheParameter(value = "mid",multiple = true) Collection<String> mainTaskIds){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("mainTaskId").in(mainTaskIds);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentTaskSubOnline::getMainTaskId));
    }

}