package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceGoal;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentPerformanceGoalService;

import java.util.Collection;

/**
 * Agent业绩目标
 *
 * @author chunlin.yu
 * @create 2017-10-26 13:32
 **/
public class AgentPerformanceGoalServiceClient implements AgentPerformanceGoalService{

    @ImportService(interfaceClass = AgentPerformanceGoalService.class)
    private AgentPerformanceGoalService agentPerformanceGoalService;

    @Override
    public MapMessage inserts(Collection<AgentPerformanceGoal> agentPerformanceGoals) {
        return agentPerformanceGoalService.inserts(agentPerformanceGoals);
    }

    @Override
    public MapMessage updates(Collection<AgentPerformanceGoal> agentPerformanceGoals) {
        return agentPerformanceGoalService.updates(agentPerformanceGoals);
    }

    @Override
    public AgentPerformanceGoal insert(AgentPerformanceGoal agentPerformanceGoal) {
        return agentPerformanceGoalService.insert(agentPerformanceGoal);
    }

    @Override
    public AgentPerformanceGoal replace(AgentPerformanceGoal agentPerformanceGoal) {
        return agentPerformanceGoalService.replace(agentPerformanceGoal);
    }
}
