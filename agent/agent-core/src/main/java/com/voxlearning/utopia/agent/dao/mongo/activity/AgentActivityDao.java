package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;

import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Named
@CacheBean(type =  AgentActivity.class)
public class AgentActivityDao extends StaticCacheDimensionDocumentMongoDao<AgentActivity, String> {

    public List<AgentActivity> loadAll(){
        Criteria criteria = Criteria.where("disabled").is(false);
        return query(Query.query(criteria));
    }

    public List<AgentActivity> loadByStartDate(Date startDate){
        if(startDate == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("startDate").gte(startDate).and("disabled").is(false);
        return query(Query.query(criteria));
    }

}
