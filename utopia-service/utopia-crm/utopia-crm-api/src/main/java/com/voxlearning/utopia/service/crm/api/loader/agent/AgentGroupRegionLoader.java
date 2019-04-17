package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AgentGroupRegionLoader
 *
 * @author song.wang
 * @date 2016/12/6
 */
@ServiceVersion(version = "2016.12.05")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentGroupRegionLoader extends IPingable {

    @Idempotent
    List<AgentGroupRegion> findByGroupId(Long groupId);

    @Idempotent
    List<AgentGroupRegion> findByRegionCode(Integer regionCode);

    @Idempotent
    Map<Integer, List<AgentGroupRegion>> findByRegionCodes(Collection<Integer> regionCodes);

    @Idempotent
    List<AgentGroupRegion> findByGroupSet(Collection<Long> groupSet);

    @Idempotent
    List<AgentGroupRegion> findAll();

    @Idempotent
    Map<Long, List<AgentGroupRegion>> findByGroupIds(Collection<Long> groupIds);

}
