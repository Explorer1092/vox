package com.voxlearning.utopia.agent.persist.honeycomb;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombUser;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@CacheBean(type =  HoneycombUser.class)
public class HoneycombUserDao extends StaticCacheDimensionDocumentMongoDao<HoneycombUser, Long> {

    @CacheMethod
    public Map<Long, List<HoneycombUser>> loadByAgentUserIds(@CacheParameter(value = "uid", multiple = true) Collection<Long> agentUserIds){
        if(CollectionUtils.isEmpty(agentUserIds)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("agentUserId").in(agentUserIds);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(HoneycombUser::getAgentUserId));
    }

    public void unsetAgentUserId(Long id){
        HoneycombUser honeycombUser = load(id);
        if(honeycombUser == null){
            return;
        }
        Update update = new Update();
        update.unset("agentUserId");

        Criteria criteria = Criteria.where("_id").is(id);
        executeUpdateMany(createMongoConnection(), criteria, update);
        evictDocumentCache(honeycombUser);
    }

}
