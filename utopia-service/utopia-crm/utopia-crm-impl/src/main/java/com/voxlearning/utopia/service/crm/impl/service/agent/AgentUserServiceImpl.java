package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentUserService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentUserPersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * AgentUserServiceImpl
 *
 * @author song.wang
 * @date 2016/12/5
 */
@Named
@Service(interfaceClass = AgentUserService.class)
@ExposeService(interfaceClass = AgentUserService.class)
public class AgentUserServiceImpl extends SpringContainerSupport implements AgentUserService {

    @Inject
    private AgentUserPersistence agentUserPersistence;

    @Override
    public Long persist(AgentUser agentUser) {
        return agentUserPersistence.persist(agentUser);
    }

    @Override
    public Boolean update(Long userId, AgentUser agentUser) {
        return agentUserPersistence.update(userId, agentUser);
    }

    @Override
    public int delete(Long id) {
        return agentUserPersistence.delete(id);
    }
}
