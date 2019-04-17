package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceServiceRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentPerformanceServiceRecordLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentPerformanceServiceRecordDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2017-10-30 20:07
 **/
@Named
@Service(interfaceClass = AgentPerformanceServiceRecordLoader.class)
@ExposeService(interfaceClass = AgentPerformanceServiceRecordLoader.class)
public class AgentPerformanceServiceRecordLoderImpl  extends SpringContainerSupport implements AgentPerformanceServiceRecordLoader {

    @Inject
    AgentPerformanceServiceRecordDao agentPerformanceServiceRecordDao;

    @Override
    public List<AgentPerformanceServiceRecord> load(Integer month, Long targetId, AgentPerformanceGoalType agentPerformanceGoalType) {
        return agentPerformanceServiceRecordDao.load(month,targetId,agentPerformanceGoalType);
    }
}
