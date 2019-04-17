package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentGroupRegionLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AgentGroupRegionLoaderClient
 *
 * @author song.wang
 * @date 2016/12/6
 */
public class AgentGroupRegionLoaderClient implements AgentGroupRegionLoader {

    @ImportService(interfaceClass = AgentGroupRegionLoader.class)
    private AgentGroupRegionLoader remoteReference;

    @Override
    public List<AgentGroupRegion> findByGroupId(Long groupId) {
        return remoteReference.findByGroupId(groupId);
    }

    @Override
    public List<AgentGroupRegion> findByRegionCode(Integer regionCode) {
        return remoteReference.findByRegionCode(regionCode);
    }

    @Override
    public Map<Integer, List<AgentGroupRegion>> findByRegionCodes(Collection<Integer> regionCodes) {
        if (CollectionUtils.isEmpty(regionCodes)) {
            return Collections.emptyMap();
        }
        return remoteReference.findByRegionCodes(regionCodes);
    }

    @Override
    public List<AgentGroupRegion> findByGroupSet(Collection<Long> groupSet) {
        if (CollectionUtils.isEmpty(groupSet)) {
            return Collections.emptyList();
        }
        return remoteReference.findByGroupSet(groupSet);
    }

    @Override
    public List<AgentGroupRegion> findAll() {
        return remoteReference.findAll();
    }

    @Override
    public Map<Long, List<AgentGroupRegion>> findByGroupIds(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.findByGroupIds(groupIds);
    }
}
