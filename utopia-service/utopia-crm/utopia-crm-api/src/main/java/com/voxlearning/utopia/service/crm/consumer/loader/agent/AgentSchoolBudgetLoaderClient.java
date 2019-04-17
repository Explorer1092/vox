package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentSchoolBudget;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentSchoolBudgetLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author chunlin.yu
 * @create 2017-08-08 10:35
 **/
public class AgentSchoolBudgetLoaderClient implements AgentSchoolBudgetLoader {

    @ImportService(interfaceClass =  AgentSchoolBudgetLoader.class)
    private AgentSchoolBudgetLoader agentSchoolBudgetLoader;

    @Override
    public List<AgentSchoolBudget> loadBySchoolId(Long schoolId) {
        return agentSchoolBudgetLoader.loadBySchoolId(schoolId);
    }

    @Override
    public List<AgentSchoolBudget> loadByMonth(Integer month) {
        return agentSchoolBudgetLoader.loadByMonth(month);
    }

    @Override
    public Map<Long, AgentSchoolBudget> loadBySchoolsAndMonth(Collection<Long> schoolIds, Integer month) {
        return agentSchoolBudgetLoader.loadBySchoolsAndMonth(schoolIds, month);
    }
}
