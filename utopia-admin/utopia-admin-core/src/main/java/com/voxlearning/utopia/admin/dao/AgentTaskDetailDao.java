package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.agent.AgentTaskDetail;

import javax.inject.Named;
import java.util.Collection;

/**
 * 市场部门的
 * Created by yaguang.wang on 2016/9/19.
 */
@Named
public class AgentTaskDetailDao extends AlpsStaticMongoDao<AgentTaskDetail, String> {
    @Override
    protected void calculateCacheDimensions(AgentTaskDetail document, Collection<String> dimensions) {

    }
}
