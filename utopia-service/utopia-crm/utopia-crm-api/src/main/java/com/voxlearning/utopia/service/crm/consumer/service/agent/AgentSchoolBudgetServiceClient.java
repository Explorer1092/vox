package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentSchoolBudget;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentSchoolBudgetService;

import java.util.Collection;

/**
 * @author chunlin.yu
 * @create 2017-08-08 10:33
 **/
public class AgentSchoolBudgetServiceClient implements AgentSchoolBudgetService {

    @ImportService(interfaceClass = AgentSchoolBudgetService.class)
    private AgentSchoolBudgetService agentSchoolBudgetService;

    @Override
    public MapMessage updates(Collection<AgentSchoolBudget> agentSchoolBudgets) {
        return agentSchoolBudgetService.updates(agentSchoolBudgets);
    }

    @Override
    public MapMessage inserts(Collection<AgentSchoolBudget> agentSchoolBudgets) {
        return agentSchoolBudgetService.inserts(agentSchoolBudgets);
    }
}
