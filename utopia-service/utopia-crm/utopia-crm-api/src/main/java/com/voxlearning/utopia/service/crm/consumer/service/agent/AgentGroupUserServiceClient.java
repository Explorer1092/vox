package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentGroupUserService;

/**
 * AgentGroupUserServiceClient
 *
 * @author song.wang
 * @date 2016/12/5
 */
public class AgentGroupUserServiceClient implements AgentGroupUserService {

    @ImportService(interfaceClass = AgentGroupUserService.class)
    private AgentGroupUserService remoteReference;

    @Override
    public Long persist(AgentGroupUser groupUser) {
        return remoteReference.persist(groupUser);
    }

    @Override
    public Boolean update(Long groupUserId, AgentGroupUser groupUser) {
        return remoteReference.update(groupUserId, groupUser);
    }

    @Override
    public int delete(Long id) {
        return remoteReference.delete(id);
    }
}
