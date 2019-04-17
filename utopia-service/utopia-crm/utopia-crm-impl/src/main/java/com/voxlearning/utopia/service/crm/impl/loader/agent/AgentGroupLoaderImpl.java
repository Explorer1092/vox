package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentGroupLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentGroupPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentGroupLoaderImpl
 *
 * @author song.wang
 * @date 2016/12/6
 */
@Named
@Service(interfaceClass = AgentGroupLoader.class)
@ExposeService(interfaceClass = AgentGroupLoader.class)
public class AgentGroupLoaderImpl extends SpringContainerSupport implements AgentGroupLoader {

    @Inject
    private AgentGroupPersistence agentGroupPersistence;

    @Override
    public AgentGroup load(Long groupId) {
        AgentGroup group = agentGroupPersistence.load(groupId);
        return group != null && !group.isDisabledTrue()? group : null;
    }

    @Override
    public Map<Long, AgentGroup> loads(Collection<Long> groupIds) {
        if(CollectionUtils.isEmpty(groupIds)){
            return Collections.emptyMap();
        }
        Map<Long, AgentGroup> groupMap = agentGroupPersistence.loads(groupIds);
        return groupMap.values().stream().filter(p -> p != null && !p.isDisabledTrue()).collect(Collectors.toMap(AgentGroup::getId, Function.identity(), ((o1, o2) -> o1)));
    }

    @Override
    public List<AgentGroup> findAllGroups() {
        return agentGroupPersistence.findAllGroups();
    }

    @Override
    public List<AgentGroup> findByParentId(Long parentId) {
        return agentGroupPersistence.findByParentId(parentId);
    }

    @Override
    public Map<Long, List<AgentGroup>> findByParentIds(Collection<Long> parentIds) {
        return agentGroupPersistence.findByParentIds(parentIds);
    }

    @Override
    public List<AgentGroup> findByRoleId(Integer roleId) {
        return agentGroupPersistence.findByRoleId(roleId);
    }

    @Override
    public AgentGroup findByGroupName(String groupName) {
        return agentGroupPersistence.findByGroupName(groupName);
    }

    @Override
    public AgentGroup loadDisabledGroup(Long groupId) {
        AgentGroup group = agentGroupPersistence.load(groupId);
        return group != null && group.isDisabledTrue()? group : null;
    }
}
