package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentGroupUserLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AgentGroupUserLoaderClient
 *
 * @author song.wang
 * @date 2016/12/6
 */
public class AgentGroupUserLoaderClient implements AgentGroupUserLoader {

    @ImportService(interfaceClass = AgentGroupUserLoader.class)
    private AgentGroupUserLoader remoteReference;

    @Override
    public List<AgentGroupUser> findAll() {
        return remoteReference.findAll();
    }

    @Override
    public List<AgentGroupUser> findByUserId(Long userId) {
        return remoteReference.findByUserId(userId);
    }

    @Override
    public Map<Long, List<AgentGroupUser>> findByUserIds(Collection<Long> userIds) {
        return remoteReference.findByUserIds(userIds);
    }

    @Override
    public List<AgentGroupUser> findByGroupId(Long groupId) {
        return remoteReference.findByGroupId(groupId);
    }

    @Override
    public Map<Long, List<AgentGroupUser>> findByGroupIds(Collection<Long> groupIds) {
        return remoteReference.findByGroupIds(groupIds);
    }

    @Override
    public List<AgentGroupUser> findByRoleId(Integer userRoleId) {
        return remoteReference.findByRoleId(userRoleId);
    }
}
