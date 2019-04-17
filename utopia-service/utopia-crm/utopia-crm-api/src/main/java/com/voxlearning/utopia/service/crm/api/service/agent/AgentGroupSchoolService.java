package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupSchool;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * AgentGroupSchoolService
 *
 * @author song.wang
 * @date 2017/8/7
 */
@ServiceVersion(version = "20170814")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AgentGroupSchoolService extends IPingable{

    @Idempotent
    Integer deleteByGroupId(Long groupId);

    @Idempotent
    Integer deleteByGroupAndRegion(Long groupId, Integer regionCode);

    Integer deleteBySchoolId(Long schoolId);

    MapMessage insert(AgentGroupSchool agentGroupSchool);

    MapMessage update(AgentGroupSchool agentGroupSchool);

    Integer deleteByGroupAndRegions(Long groupId, Collection<Integer> regionCode);
}
