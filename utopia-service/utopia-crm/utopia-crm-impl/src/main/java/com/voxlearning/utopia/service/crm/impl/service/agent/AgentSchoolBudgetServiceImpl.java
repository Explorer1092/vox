package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentSchoolBudget;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentSchoolBudgetService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentSchoolBudgetDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chunlin.yu
 * @create 2017-08-08 10:43
 **/

@Named
@Service(interfaceClass = AgentSchoolBudgetService.class)
@ExposeService(interfaceClass = AgentSchoolBudgetService.class)
public class AgentSchoolBudgetServiceImpl extends SpringContainerSupport implements AgentSchoolBudgetService {

    @Inject
    private AgentSchoolBudgetDao agentSchoolBudgetDao;

    @Override
    public MapMessage updates(Collection<AgentSchoolBudget> agentSchoolBudgets) {
        if (CollectionUtils.isEmpty(agentSchoolBudgets)){
            return MapMessage.errorMessage("参数为空");
        }
        agentSchoolBudgets.forEach(item ->{
            if (null != item.getId()){
                agentSchoolBudgetDao.replace(item);
            }
        });

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage inserts(Collection<AgentSchoolBudget> agentSchoolBudgets) {
        if (CollectionUtils.isEmpty(agentSchoolBudgets)){
            return MapMessage.errorMessage("参数为空");
        }
        agentSchoolBudgetDao.inserts(agentSchoolBudgets);
        return MapMessage.successMessage();
    }


}
