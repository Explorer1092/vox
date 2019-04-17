package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentGroupLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AgentGroupLoaderClient
 *
 * @author song.wang
 * @date 2016/12/6
 */
public class AgentGroupLoaderClient implements AgentGroupLoader {

    @ImportService(interfaceClass = AgentGroupLoader.class)
    private AgentGroupLoader remoteReference;

    @Override
    public AgentGroup load(Long groupId) {
        return remoteReference.load(groupId);
    }

    @Override
    public Map<Long, AgentGroup> loads(Collection<Long> groupIds) {
        return remoteReference.loads(groupIds);
    }

    @Override
    public List<AgentGroup> findAllGroups() {
        return remoteReference.findAllGroups();
    }

    @Override
    public List<AgentGroup> findByParentId(Long parentId) {
        return remoteReference.findByParentId(parentId);
    }

    @Override
    public Map<Long, List<AgentGroup>> findByParentIds(Collection<Long> parentIds) {
        return remoteReference.findByParentIds(parentIds);
    }

    @Override
    public List<AgentGroup> findByRoleId(Integer roleId) {
        return remoteReference.findByRoleId(roleId);
    }

    @Override
    public AgentGroup findByGroupName(String groupName) {
        return remoteReference.findByGroupName(groupName);
    }

    @Override
    public AgentGroup loadDisabledGroup(Long groupId) {
        return remoteReference.loadDisabledGroup(groupId);
    }
}
