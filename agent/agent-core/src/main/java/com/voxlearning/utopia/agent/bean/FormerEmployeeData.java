package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * FormerEmployeeData 离职人员数据， 用于计算离职人员工资
 *
 * @author song.wang
 * @date 2016/9/27
 */
@Getter
@Setter
public class FormerEmployeeData {
    private Long userId;
    private AgentRoleType roleType; // 用户角色
    private AgentCityLevelType cityLevelType; // 城市级别
    private Boolean isJuniorAgentModel; // 小学是否是代理模式
    private Boolean isMiddleAgentModel;// 中学学是否是代理模式
    private List<Long> schoolIdList; // 用户负责的学校列表
}
