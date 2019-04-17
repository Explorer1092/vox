package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by song.wang on 2016/12/5.
 */
@ServiceVersion(version = "2016.12.05")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AgentGroupService extends IPingable {

    Long persist(AgentGroup group);

    @Deprecated
    Boolean update(Long groupId, AgentGroup group);

    AgentGroup replace(AgentGroup group);

    void updateGroupRole(Long groupId, AgentGroupRoleType roleType);



}
