package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceGoal;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentPerformanceGoalService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentPerformanceGoalDao;
import javassist.bytecode.stackmap.MapMaker;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * @author chunlin.yu
 * @create 2017-10-26 14:37
 **/
@Named
@Service(interfaceClass = AgentPerformanceGoalService.class)
@ExposeService(interfaceClass = AgentPerformanceGoalService.class)
public class AgentPerformanceGoalServiceImpl implements AgentPerformanceGoalService {

    @Inject
    AgentPerformanceGoalDao agentPerformanceGoalDao;

    @Override
    public MapMessage inserts(Collection<AgentPerformanceGoal> agentPerformanceGoals) {
        if (CollectionUtils.isEmpty(agentPerformanceGoals)){
            return MapMessage.errorMessage("参数不能为空");
        }
        agentPerformanceGoals.forEach(item -> {
            if (null == item.getDisabled()){
                item.setDisabled(false);
            }
        });
        agentPerformanceGoalDao.inserts(agentPerformanceGoals);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updates(Collection<AgentPerformanceGoal> agentPerformanceGoals) {
        if (CollectionUtils.isEmpty(agentPerformanceGoals)){
            return MapMessage.errorMessage("参数不能为空");
        }
        agentPerformanceGoals.forEach(item ->{
            if (null != item.getId()){
                if (null == item.getDisabled()){
                    item.setDisabled(false);
                }
                agentPerformanceGoalDao.replace(item);
            }
        });
        return MapMessage.successMessage();
    }

    @Override
    public AgentPerformanceGoal insert(AgentPerformanceGoal agentPerformanceGoal) {
        if (null == agentPerformanceGoal.getDisabled()){
            agentPerformanceGoal.setDisabled(false);
        }
        agentPerformanceGoalDao.insert(agentPerformanceGoal);
        return agentPerformanceGoal;
    }

    @Override
    public AgentPerformanceGoal replace(AgentPerformanceGoal agentPerformanceGoal) {
        if (null == agentPerformanceGoal.getDisabled()){
            agentPerformanceGoal.setDisabled(false);
        }
        return agentPerformanceGoalDao.replace(agentPerformanceGoal);
    }

}
