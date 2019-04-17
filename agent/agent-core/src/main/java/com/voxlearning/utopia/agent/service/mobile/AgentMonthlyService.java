package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.utopia.agent.dao.mongo.AgentMonthlyDao;
import com.voxlearning.utopia.agent.persist.entity.AgentMonthly;
import com.voxlearning.utopia.agent.service.AbstractAgentService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * AgentMonthlyService
 *
 * @author song.wang
 * @date 2016/10/26
 */
@Named
public class AgentMonthlyService extends AbstractAgentService {
    @Inject
    private AgentMonthlyDao agentMonthlyDao;

    public AgentMonthly findByUserAndMonth(Long userId, Integer month){
        return agentMonthlyDao.findByUserAndMonth(userId, month);
    }
}
