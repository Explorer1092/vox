package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AgentGroupLoader
 *
 * @author song.wang
 * @date 2016/12/6
 */
@ServiceVersion(version = "2016.12.05")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentGroupLoader extends IPingable {

    @Idempotent
    AgentGroup load(Long groupId);

    @Idempotent
    Map<Long, AgentGroup> loads(Collection<Long> groupIds);

    @Idempotent
    List<AgentGroup> findAllGroups();

    @Idempotent
    List<AgentGroup> findByParentId(Long parentId);

    @Idempotent
    Map<Long, List<AgentGroup>> findByParentIds(Collection<Long> parentIds);

    @Idempotent
    List<AgentGroup> findByRoleId(Integer roleId);

    @Idempotent
    AgentGroup findByGroupName(String groupName);


    @Idempotent
    AgentGroup loadDisabledGroup(Long groupId);

}
