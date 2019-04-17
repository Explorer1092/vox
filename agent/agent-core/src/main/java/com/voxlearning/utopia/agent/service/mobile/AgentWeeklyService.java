package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.utopia.agent.dao.mongo.AgentWeeklyDao;
import com.voxlearning.utopia.agent.persist.entity.AgentWeekly;
import com.voxlearning.utopia.agent.service.AbstractAgentService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * AgentWeeklyService
 *
 * @author song.wang
 * @date 2016/8/15
 */
@Named
public class AgentWeeklyService extends AbstractAgentService {

    @Inject
    private AgentWeeklyDao agentWeeklyDao;

    public AgentWeekly findByUserAndDay(Long userId, Integer day){
        return agentWeeklyDao.findByUserAndDay(userId, day);
    }


}
