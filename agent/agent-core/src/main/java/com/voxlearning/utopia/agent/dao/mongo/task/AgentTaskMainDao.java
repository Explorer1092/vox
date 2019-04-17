package com.voxlearning.utopia.agent.dao.mongo.task;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.task.AgentTaskMain;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentTaskMain.class)
public class AgentTaskMainDao extends StaticCacheDimensionDocumentMongoDao<AgentTaskMain, String> {

    @CacheMethod
    public Map<String,AgentTaskMain> loadByIds(@CacheParameter(value = "id",multiple = true) Collection<String> ids){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("_id").in(ids);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(AgentTaskMain::getId, Function.identity(), (o1, o2) -> o1));
    }

    public List<AgentTaskMain> loadByDate(Date date){
        Criteria criteria = Criteria.where("disabled").is(false);
        if (null != date){
            criteria.and("createTime").gte(date);
        }else {
            //查询近半年（6个月）数据
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MONTH, -6);
            Date lastSixMonthTime = calendar.getTime();
            criteria.and("createTime").gte(lastSixMonthTime);
        }
        return query(Query.query(criteria));
    }
}