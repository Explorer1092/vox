package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupSchool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AgentGroupSchoolLoader
 *
 * @author song.wang
 * @date 2017/8/7
 */
@ServiceVersion(version = "20170807")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentGroupSchoolLoader extends IPingable{

    @Idempotent
    List<AgentGroupSchool> findByGroupId(Long groupId);

    @Idempotent
    Map<Long, List<AgentGroupSchool>> findByGroupIds(Collection<Long> groupIds);

    @Idempotent
    AgentGroupSchool findBySchoolId(Long schoolId);

    @Idempotent
    Map<Long, AgentGroupSchool> findBySchoolIds(Collection<Long> schoolIds);

}
