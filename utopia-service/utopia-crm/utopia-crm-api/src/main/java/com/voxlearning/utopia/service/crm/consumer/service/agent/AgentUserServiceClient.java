package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentUserService;

/**
 * AgentUserServiceClient
 *
 * @author song.wang
 * @date 2016/12/5
 */
public class AgentUserServiceClient implements AgentUserService {

    @ImportService(interfaceClass = AgentUserService.class)
    private AgentUserService remoteReference;

    @Override
    public Long persist(AgentUser agentUser) {
        return remoteReference.persist(agentUser);
    }

    @Override
    public Boolean update(Long userId, AgentUser agentUser) {
        return remoteReference.update(userId, agentUser);
    }

    @Override
    public int delete(Long id) {
        return remoteReference.delete(id);
    }
}
