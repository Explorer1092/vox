package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.agent.persist.entity.loginlog.AgentUserLoginLog;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by yaguang.wang
 * on 2017/5/26.
 */
@Named
@CacheBean(type = AgentUserLoginLog.class)
public class AgentUserLoginLogDao extends AlpsStaticMongoDao<AgentUserLoginLog, String> {
    @Override
    protected void calculateCacheDimensions(AgentUserLoginLog document, Collection<String> dimensions) {

    }
}
