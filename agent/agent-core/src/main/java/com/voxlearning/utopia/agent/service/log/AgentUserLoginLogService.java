package com.voxlearning.utopia.agent.service.log;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserLoginLogDao;
import com.voxlearning.utopia.agent.persist.entity.loginlog.AgentUserLoginLog;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by yaguang.wang
 * on 2017/5/26.
 */
@Named
public class AgentUserLoginLogService extends SpringContainerSupport {
    @Inject AgentUserLoginLogDao agentUserLoginLogDao;

    public String insertLoginLog(AgentUserLoginLog log) {
        agentUserLoginLogDao.insert(log);
        return log.getId();
    }
}
