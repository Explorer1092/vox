package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentGroupService;

/**
 * AgentGroupServiceClient
 *
 * @author song.wang
 * @date 2016/12/5
 */
public class AgentGroupServiceClient implements AgentGroupService {

    @ImportService(interfaceClass = AgentGroupService.class)
    private AgentGroupService remoteReference;

    @Override
    public Long persist(AgentGroup group) {
        return remoteReference.persist(group);
    }

    @Deprecated
    @Override
    public Boolean update(Long groupId, AgentGroup group) {
        return remoteReference.update(groupId, group);
    }

    @Override
    public AgentGroup replace(AgentGroup group) {
        return remoteReference.replace(group);
    }

    @Override
    public void updateGroupRole(Long groupId, AgentGroupRoleType roleType) {
        remoteReference.updateGroupRole(groupId, roleType);
    }

}
