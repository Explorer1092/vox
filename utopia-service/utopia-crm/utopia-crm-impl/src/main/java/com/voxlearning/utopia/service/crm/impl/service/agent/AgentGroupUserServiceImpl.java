package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentGroupUserService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentGroupUserPersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * AgentGroupUserServiceImpl
 *
 * @author song.wang
 * @date 2016/12/5
 */
@Named
@Service(interfaceClass = AgentGroupUserService.class)
@ExposeService(interfaceClass = AgentGroupUserService.class)
public class AgentGroupUserServiceImpl extends SpringContainerSupport implements AgentGroupUserService {

    @Inject
    private AgentGroupUserPersistence agentGroupUserPersistence;

    @Override
    public Long persist(AgentGroupUser groupUser) {
        return agentGroupUserPersistence.persist(groupUser);
    }

    @Override
    public Boolean update(Long groupUserId, AgentGroupUser groupUser) {
        return agentGroupUserPersistence.update(groupUserId, groupUser);
    }

    @Override
    public int delete(Long id) {
        return agentGroupUserPersistence.delete(id);
    }
}
