package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceServiceRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentPerformanceServiceRecordLoader;

import java.util.List;

/**
 * @author chunlin.yu
 * @create 2017-10-30 20:17
 **/
public class AgentPerformanceServiceRecordLoderClient implements AgentPerformanceServiceRecordLoader {

    @ImportService(interfaceClass = AgentPerformanceServiceRecordLoader.class)
    private AgentPerformanceServiceRecordLoader agentPerformanceServiceRecordLoader;

    @Override
    public List<AgentPerformanceServiceRecord> load(Integer month, Long targetId, AgentPerformanceGoalType agentPerformanceGoalType) {
        return agentPerformanceServiceRecordLoader.load(month,targetId,agentPerformanceGoalType);
    }
}
