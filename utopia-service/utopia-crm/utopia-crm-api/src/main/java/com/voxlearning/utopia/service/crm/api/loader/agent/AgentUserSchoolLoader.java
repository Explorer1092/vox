package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AgentUserSchoolLoader
 *
 * @author song.wang
 * @date 2016/12/6
 */
@ServiceVersion(version = "2016.12.05")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentUserSchoolLoader extends IPingable {

    @Idempotent
    List<AgentUserSchool> findAll();

    @Idempotent
    List<AgentUserSchool> findByUserId(Long userId);

    @Idempotent
    Map<Long, List<AgentUserSchool>> findByUserIds(Collection<Long> userIds);

    @Idempotent
    List<AgentUserSchool> findBySchoolId(Long schoolId);

    @Idempotent
    List<AgentUserSchool> findBySchoolIds(Collection<Long> schoolIds);

}
