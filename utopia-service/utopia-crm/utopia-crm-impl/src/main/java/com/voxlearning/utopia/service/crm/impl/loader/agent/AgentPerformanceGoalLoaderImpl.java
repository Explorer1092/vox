package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceGoal;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentPerformanceGoalLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentPerformanceGoalDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chunlin.yu
 * @create 2017-10-26 14:48
 **/
@Named
@Service(interfaceClass = AgentPerformanceGoalLoader.class)
@ExposeService(interfaceClass = AgentPerformanceGoalLoader.class)
public class AgentPerformanceGoalLoaderImpl extends SpringContainerSupport implements AgentPerformanceGoalLoader {

    @Inject
    private AgentPerformanceGoalDao agentPerformanceGoalDao;


    @Override
    public List<AgentPerformanceGoal> loadByMonth(Integer month) {
        return agentPerformanceGoalDao.loadByMonth(month);
    }

    @Override
    public List<AgentPerformanceGoal> loadConfirmedByMonth(Integer month) {
        return agentPerformanceGoalDao.loadByMonth(month,true);
    }

    @Override
    public AgentPerformanceGoal loadConfirmedByIdAndType(Long id, AgentPerformanceGoalType type, Integer month) {
        if(type == null || (type != AgentPerformanceGoalType.COUNTRY && id == null)){
            return null;
        }
        List<AgentPerformanceGoal> goalList = agentPerformanceGoalDao.loadByMonth(month,true);
        if(CollectionUtils.isEmpty(goalList)){
            return null;
        }
        return goalList.stream().filter(p -> p.getAgentPerformanceGoalType() == type)
                .filter(p -> (type == AgentPerformanceGoalType.COUNTRY) ||
                        (type == AgentPerformanceGoalType.REGION_GROUP && Objects.equals(p.getRegionGroupId(), id)) ||
                        (type == AgentPerformanceGoalType.SUB_REGION_GROUP && Objects.equals(p.getSubRegionGroupId(), id)) ||
                        (type == AgentPerformanceGoalType.USER && Objects.equals(p.getUserId(), id)))
                .findFirst().orElse(null);
    }

    @Override
    public List<AgentPerformanceGoal> loadByIdAndTypeAndBeginMonth(Long id, AgentPerformanceGoalType idType, Integer beginMonth) {
        if(idType == null || idType == null || beginMonth == null){
            return Collections.emptyList();
        }
        List<AgentPerformanceGoal> agentPerformanceGoalList = agentPerformanceGoalDao.loadByBeginMonth(beginMonth);
        switch (idType){
            case COUNTRY:
                return agentPerformanceGoalList;
            case REGION_GROUP:
                return agentPerformanceGoalList.stream().filter(item -> Objects.equals(item.getRegionGroupId(),id)).collect(Collectors.toList());
            case SUB_REGION_GROUP:
                return agentPerformanceGoalList.stream().filter(item -> Objects.equals(item.getSubRegionGroupId(),id)).collect(Collectors.toList());
            case USER:
                return agentPerformanceGoalList.stream().filter(item -> Objects.equals(item.getUserId(),id)).collect(Collectors.toList());
            default:
                break;
        }
        return Collections.emptyList();
    }


}
