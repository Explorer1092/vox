package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentSchoolBudget;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author chunlin.yu
 * @create 2017-08-08 10:27
 **/
@ServiceVersion(version = "2017.08.014")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AgentSchoolBudgetService extends IPingable {

    MapMessage updates(Collection<AgentSchoolBudget> agentSchoolBudgets);

    MapMessage inserts(Collection<AgentSchoolBudget> agentSchoolBudgets);
}
