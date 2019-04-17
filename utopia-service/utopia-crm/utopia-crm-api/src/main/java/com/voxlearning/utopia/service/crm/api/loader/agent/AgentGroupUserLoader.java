package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AgentGroupUserLoader
 *
 * @author song.wang
 * @date 2016/12/6
 */
@ServiceVersion(version = "2016.12.05")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentGroupUserLoader extends IPingable {

    @Idempotent
    List<AgentGroupUser> findAll();

    @Idempotent
    List<AgentGroupUser> findByUserId(Long userId);

    @Idempotent
    Map<Long, List<AgentGroupUser>> findByUserIds(Collection<Long> userIds);

    @Idempotent
    List<AgentGroupUser> findByGroupId(Long groupId);

    @Idempotent
    Map<Long, List<AgentGroupUser>> findByGroupIds(Collection<Long> groupIds);

    @Idempotent
    List<AgentGroupUser> findByRoleId(Integer userRoleId);

}
