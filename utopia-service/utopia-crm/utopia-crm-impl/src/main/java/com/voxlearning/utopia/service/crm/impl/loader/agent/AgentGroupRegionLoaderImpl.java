package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentGroupRegionLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentGroupRegionPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AgentGroupRegionLoaderImpl
 *
 * @author song.wang
 * @date 2016/12/6
 */
@Named
@Service(interfaceClass = AgentGroupRegionLoader.class)
@ExposeService(interfaceClass = AgentGroupRegionLoader.class)
public class AgentGroupRegionLoaderImpl extends SpringContainerSupport implements AgentGroupRegionLoader {
    @Inject
    private AgentGroupRegionPersistence agentGroupRegionPersistence;

    @Override
    public List<AgentGroupRegion> findByGroupId(Long groupId) {
        return agentGroupRegionPersistence.findByGroupId(groupId);
    }

    @Override
    public List<AgentGroupRegion> findByRegionCode(Integer regionCode) {
        return agentGroupRegionPersistence.findByRegionCode(regionCode);
    }

    @Override
    public Map<Integer, List<AgentGroupRegion>> findByRegionCodes(Collection<Integer> regionCodes) {
        return agentGroupRegionPersistence.findByRegionCodes(regionCodes);
    }

    @Override
    public List<AgentGroupRegion> findByGroupSet(Collection<Long> groupSet) {
        return agentGroupRegionPersistence.findByGroupSet(groupSet);
    }

    @Override
    public List<AgentGroupRegion> findAll() {
        return agentGroupRegionPersistence.findAll();
    }

    @Override
    public Map<Long, List<AgentGroupRegion>> findByGroupIds(Collection<Long> groupIds) {
        return agentGroupRegionPersistence.findByGroupIds(groupIds);
    }
}
