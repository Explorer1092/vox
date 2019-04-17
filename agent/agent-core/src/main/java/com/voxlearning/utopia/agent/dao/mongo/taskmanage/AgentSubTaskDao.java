package com.voxlearning.utopia.agent.dao.mongo.taskmanage;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.taskmanage.AgentSubTask;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentSubTask.class)
public class AgentSubTaskDao extends StaticCacheDimensionDocumentMongoDao<AgentSubTask, String> {

    //统计专员的 任务列表
    public long countTaskSubySchoolIds(Collection<Long> schoolIds,String mainTaskId) {
        Criteria criteria = Criteria.where("mainTaskId").is(mainTaskId);
        criteria.and("schoolId").in(schoolIds);
        Query query = Query.query(criteria);
        return count(query);
    }

    //统计专员的 任务列表
    @CacheMethod
    public Map<Long,List<AgentSubTask>> findTaskSubBySchoolIds(@CacheParameter("mid")String mainTaskId,@CacheParameter(value = "schoolId", multiple = true)Collection<Long> schoolIds) {
        Criteria criteria = Criteria.where("mainTaskId").is(mainTaskId);
        criteria.and("schoolId").in(schoolIds);
        Query query = Query.query(criteria);
        return query(query).stream().filter(p -> p.getDisabled() == false).collect(Collectors.groupingBy(AgentSubTask::getSchoolId, Collectors.toList()));
    }
    //查询市经理及以上 任务列表
    @CacheMethod
    public Map<Integer,List<AgentSubTask>> findTaskSubByRegionCodes(@CacheParameter("mid")String mainTaskId, @CacheParameter(value = "regionCode", multiple = true)Collection<Integer> regionCodes) {
        Criteria criteria = Criteria.where("mainTaskId").is(mainTaskId);
        criteria.and("regionCode").in(regionCodes);
        Query query = Query.query(criteria);
        return query(query).stream().filter(p -> p.getDisabled() == false).collect(Collectors.groupingBy(AgentSubTask::getRegionCode, Collectors.toList()));
    }

    /**
     * 根据主任务ID删除子任务
     * @param mainTaskId
     */
    public void deleteByMainTaskId(String mainTaskId){
        Update update = Update.update("disabled", true);
        Criteria criteria = Criteria.where("mainTaskId").is(mainTaskId);
        executeUpdateMany(createMongoConnection(), criteria, update);
        // 清除缓存
        Query query = Query.query(criteria);
        List<AgentSubTask> list = query(query);
        evictDocumentCache(list);
    }

    //查询专员任务老师列表
    public List<AgentSubTask> findTaskSubyMainTaskId(String mainTaskId) {
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("mainTaskId").is(mainTaskId);
        Query query = Query.query(criteria);
        return query(query);
    }

    public void updateIfHomework(Collection<String> mainTaskIds,Long teacherId){
        Update update = Update.update("ifHomework", true);
        update.set("homeworkTime",new Date());
        Criteria criteria = Criteria.where("mainTaskId").in(mainTaskIds);
        criteria.and("teacherId").is(teacherId);
        criteria.and("ifHomework").is(false);
        criteria.and("disabled").is(false);

        Query query = Query.query(criteria);
        List<AgentSubTask> list = query(query);

        executeUpdateMany(createMongoConnection(), criteria, update);
        // 清除缓存
        evictDocumentCache(list);
    }

    public void updateFollow(Collection<String> mainTaskIds,Long teacherId){
        Update update = Update.update("ifFollowUp", true);
        Criteria criteria = Criteria.where("mainTaskId").in(mainTaskIds);
        criteria.and("teacherId").is(teacherId);
        criteria.and("ifFollowUp").is(false);
        criteria.and("disabled").is(false);

        Query query = Query.query(criteria);
        List<AgentSubTask> list = query(query);

        executeUpdateMany(createMongoConnection(), criteria, update);
        // 清除缓存
        evictDocumentCache(list);
    }

    @CacheMethod
    public Map<String,List<AgentSubTask>> findSubTaskByMainTaskIds(@CacheParameter(value = "mid", multiple = true)Collection<String> mainTaskIds) {
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("mainTaskId").in(mainTaskIds);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.groupingBy(AgentSubTask::getMainTaskId, Collectors.toList()));
    }
}