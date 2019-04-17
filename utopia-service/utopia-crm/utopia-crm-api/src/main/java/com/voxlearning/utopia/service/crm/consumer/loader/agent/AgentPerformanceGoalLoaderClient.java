package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceGoal;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentPerformanceGoalLoader;

import java.util.List;

/**
 * @author chunlin.yu
 * @create 2017-10-26 14:16
 **/
public class AgentPerformanceGoalLoaderClient implements AgentPerformanceGoalLoader {

    @ImportService(interfaceClass = AgentPerformanceGoalLoader.class)
    private AgentPerformanceGoalLoader agentPerformanceGoalLoader;

    @Override
    public List<AgentPerformanceGoal> loadByMonth(Integer month) {
        return agentPerformanceGoalLoader.loadByMonth(month);
    }

    @Override
    public List<AgentPerformanceGoal> loadConfirmedByMonth(Integer month) {
        return agentPerformanceGoalLoader.loadConfirmedByMonth(month);
    }

    @Override
    public AgentPerformanceGoal loadConfirmedByIdAndType(Long id, AgentPerformanceGoalType type, Integer month) {
        return agentPerformanceGoalLoader.loadConfirmedByIdAndType(id, type, month);
    }

    @Override
    public List<AgentPerformanceGoal> loadByIdAndTypeAndBeginMonth(Long id, AgentPerformanceGoalType idType, Integer beginMonth) {
        return agentPerformanceGoalLoader.loadByIdAndTypeAndBeginMonth(id, idType, beginMonth);
    }
}
