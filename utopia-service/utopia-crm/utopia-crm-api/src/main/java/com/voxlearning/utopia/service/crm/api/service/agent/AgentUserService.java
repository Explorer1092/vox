package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

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
public interface AgentUserService extends IPingable {

    Long persist(AgentUser agentUser);

    Boolean update(Long userId, AgentUser agentUser);

    int delete(final Long id);

}
