package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceServiceRecord;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentPerformanceServiceRecordService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentPerformanceServiceRecordDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2017-10-30 20:05
 **/
@Named
@Service(interfaceClass = AgentPerformanceServiceRecordService.class)
@ExposeService(interfaceClass = AgentPerformanceServiceRecordService.class)
public class AgentPerformanceServiceRecordServiceImpl implements AgentPerformanceServiceRecordService {
    @Inject
    AgentPerformanceServiceRecordDao agentPerformanceServiceRecordDao;

    @Override
    public AgentPerformanceServiceRecord insert(AgentPerformanceServiceRecord agentPerformanceServiceRecord) {
        agentPerformanceServiceRecordDao.insert(agentPerformanceServiceRecord);
        return agentPerformanceServiceRecord;
    }
}
