package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentGroupRegionService;

/**
 * AgentGroupRegionServiceClient
 *
 * @author song.wang
 * @date 2016/12/5
 */
public class AgentGroupRegionServiceClient implements AgentGroupRegionService {

    @ImportService(interfaceClass = AgentGroupRegionService.class)
    private AgentGroupRegionService remoteReference;

    @Override
    public Long persist(AgentGroupRegion groupRegion) {
        return remoteReference.persist(groupRegion);
    }

    @Override
    public Boolean update(Long groupRegionId, AgentGroupRegion groupRegion) {
        return remoteReference.update(groupRegionId, groupRegion);
    }

    @Override
    public int delete(Long id) {
        return remoteReference.delete(id);
    }

}
