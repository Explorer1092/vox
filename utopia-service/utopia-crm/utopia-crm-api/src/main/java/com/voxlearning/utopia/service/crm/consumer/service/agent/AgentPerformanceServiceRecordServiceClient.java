package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceServiceRecord;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentPerformanceServiceRecordService;

/**
 * @author chunlin.yu
 * @create 2017-10-30 20:18
 **/
public class AgentPerformanceServiceRecordServiceClient implements AgentPerformanceServiceRecordService {

    @ImportService(interfaceClass = AgentPerformanceServiceRecordService.class)
    private AgentPerformanceServiceRecordService agentPerformanceServiceRecordService;

    @Override
    public AgentPerformanceServiceRecord insert(AgentPerformanceServiceRecord agentPerformanceServiceRecord) {
        return agentPerformanceServiceRecordService.insert(agentPerformanceServiceRecord);
    }
}
