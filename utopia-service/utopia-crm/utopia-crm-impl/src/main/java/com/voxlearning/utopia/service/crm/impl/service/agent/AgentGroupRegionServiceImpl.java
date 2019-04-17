package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentGroupRegionService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentGroupRegionPersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * AgentGroupRegionServiceImpl
 *
 * @author song.wang
 * @date 2016/12/5
 */
@Named
@Service(interfaceClass = AgentGroupRegionService.class)
@ExposeService(interfaceClass = AgentGroupRegionService.class)
public class AgentGroupRegionServiceImpl extends SpringContainerSupport implements AgentGroupRegionService {

    @Inject
    private AgentGroupRegionPersistence agentGroupRegionPersistence;

    @Override
    public Long persist(AgentGroupRegion groupRegion) {
        return agentGroupRegionPersistence.persist(groupRegion);
    }

    @Override
    public Boolean update(Long groupRegionId, AgentGroupRegion groupRegion) {
        return agentGroupRegionPersistence.update(groupRegionId, groupRegion);
    }

    @Override
    public int delete(Long id) {
        return agentGroupRegionPersistence.delete(id);
    }
}
