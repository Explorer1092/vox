package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.agent.constants.AgentDateConfigType;
import com.voxlearning.utopia.agent.persist.entity.AgentDateConfig;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by yagaung.wang
 * on 2017/3/27.
 */
@Named
@CacheBean(type = AgentDateConfig.class)
public class AgentDateConfigDao extends AlpsStaticMongoDao<AgentDateConfig, String> {

    @Override
    protected void calculateCacheDimensions(AgentDateConfig document, Collection<String> dimensions) {

    }

    public AgentDateConfig loadDateConfigByType(AgentDateConfigType type) {
        Criteria criteria = Criteria.where("configType").is(type);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }
}
