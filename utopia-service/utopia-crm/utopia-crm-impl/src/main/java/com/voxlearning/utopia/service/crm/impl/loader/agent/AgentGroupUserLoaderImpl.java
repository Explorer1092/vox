package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentGroupUserLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentGroupUserPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AgentGroupUserLoaderImpl
 *
 * @author song.wang
 * @date 2016/12/6
 */

@Named
@Service(interfaceClass = AgentGroupUserLoader.class)
@ExposeService(interfaceClass = AgentGroupUserLoader.class)
public class AgentGroupUserLoaderImpl extends SpringContainerSupport implements AgentGroupUserLoader {
    @Inject
    private AgentGroupUserPersistence agentGroupUserPersistence;

    @Override
    public List<AgentGroupUser> findAll() {
        return agentGroupUserPersistence.findAll();
    }

    @Override
    public List<AgentGroupUser> findByUserId(Long userId) {
        return agentGroupUserPersistence.findByUserId(userId);
    }

    @Override
    public Map<Long, List<AgentGroupUser>> findByUserIds(Collection<Long> userIds) {
        return agentGroupUserPersistence.findByUserIds(userIds);
    }

    @Override
    public List<AgentGroupUser> findByGroupId(Long groupId) {
        return agentGroupUserPersistence.findByGroupId(groupId);
    }

    @Override
    public Map<Long, List<AgentGroupUser>> findByGroupIds(Collection<Long> groupIds) {
        return agentGroupUserPersistence.findByGroupIds(groupIds);
    }

    @Override
    public List<AgentGroupUser> findByRoleId(Integer userRoleId) {
        return agentGroupUserPersistence.findByRoleId(userRoleId);
    }
}
