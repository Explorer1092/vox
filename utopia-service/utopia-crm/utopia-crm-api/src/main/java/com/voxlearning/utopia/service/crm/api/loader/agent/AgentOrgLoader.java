package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author song.wang
 * @date 2016/12/23
 */
@ServiceVersion(version = "20170210")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentOrgLoader extends IPingable {

    @Idempotent
    Boolean isDictSchool(Long schoolId);

    @Idempotent
    AgentGroup loadAgentGroupByUserId(Long userId);

    List<SchoolLevel> fetchUserServeSchoolLevels(Long userId);
}
