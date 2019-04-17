package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentSchoolBudget;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentSchoolBudgetLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentSchoolBudgetDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author chunlin.yu
 * @create 2017-08-08 10:42
 **/
@Named
@Service(interfaceClass = AgentSchoolBudgetLoader.class)
@ExposeService(interfaceClass = AgentSchoolBudgetLoader.class)
public class AgentSchoolBudgetLoaderImpl extends SpringContainerSupport implements AgentSchoolBudgetLoader {
    @Inject
    private AgentSchoolBudgetDao agentSchoolBudgetDao;


    @Override
    public List<AgentSchoolBudget> loadBySchoolId(Long schoolId) {
        return agentSchoolBudgetDao.loadBySchoolId(schoolId);
    }

    @Override
    public List<AgentSchoolBudget> loadByMonth(Integer month) {
        return agentSchoolBudgetDao.loadByMonth(month);
    }

    @Override
    public Map<Long, AgentSchoolBudget> loadBySchoolsAndMonth(Collection<Long> schoolIds, Integer month) {
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        return agentSchoolBudgetDao.loadBySchoolsAndMonth(schoolIds, month);
    }


}
