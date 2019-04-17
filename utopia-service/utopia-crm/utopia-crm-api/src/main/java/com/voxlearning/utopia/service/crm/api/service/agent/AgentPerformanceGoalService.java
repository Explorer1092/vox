package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceGoal;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Agent业绩目标Service
 *
 * @author chunlin.yu
 * @create 2017-10-26 13:33
 **/
@ServiceVersion(version = "2017.10.26")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentPerformanceGoalService extends IPingable {
    /**
     * 批量导入
     * @param agentPerformanceGoals
     * @return
     */
    MapMessage inserts(Collection<AgentPerformanceGoal> agentPerformanceGoals);

    /**
     * 批量更新
     * @param agentPerformanceGoals
     * @return
     */
    MapMessage updates(Collection<AgentPerformanceGoal> agentPerformanceGoals);

    AgentPerformanceGoal insert(AgentPerformanceGoal agentPerformanceGoal);

    AgentPerformanceGoal replace(AgentPerformanceGoal agentPerformanceGoal);
}
