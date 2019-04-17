package com.voxlearning.utopia.agent.bean.workrecord;

import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chunlin.yu
 * @create 2018-01-23 16:55
 **/
@Setter
@Getter
public class PersonalWorkloadStatisticsItem extends WorkloadStatisticsItem{

    public PersonalWorkloadStatisticsItem(Long userId,Integer idType,String userName,Double workload,String groupName,AgentRoleType agentRoleType){
        super(userId,idType,userName,workload);
        setGroupName(groupName);
        setAgentRoleType(agentRoleType);
    }

    /**
     * 用户类型
     */
    private AgentRoleType agentRoleType;

    /**
     * 部门名称
     */
    private String groupName;
}
