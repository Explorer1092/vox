package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentGroupService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentGroupPersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * AgentGroupServiceImpl
 *
 * @author song.wang
 * @date 2016/12/5
 */
@Named
@Service(interfaceClass = AgentGroupService.class)
@ExposeService(interfaceClass = AgentGroupService.class)
public class AgentGroupServiceImpl extends SpringContainerSupport implements AgentGroupService {

    @Inject
    private AgentGroupPersistence agentGroupPersistence;

    @Override
    public Long persist(AgentGroup group) {
        agentGroupPersistence.insert(group);
        return group.getId();
    }

    @Deprecated
    @Override
    public Boolean update(Long groupId, AgentGroup group) {
        agentGroupPersistence.replace(group);
        return true;
    }

    @Override
    public AgentGroup replace(AgentGroup group){
        return agentGroupPersistence.replace(group);
    }

    @Override
    public void updateGroupRole(Long groupId, AgentGroupRoleType roleType) {
        agentGroupPersistence.updateGroupRole(groupId, roleType);
    }
}
