package com.voxlearning.utopia.agent.persist.entity.material;

import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author deliang.che
 * @since 2018/7/19
 **/
@Getter
@Setter
public class AgentMaterialCostExportData implements Serializable,ExportAble {

    private static final long serialVersionUID = 2460681104195781731L;
    private String schoolTerm;//学期
    private String groupName;//部门名称
    private Double groupBudget;  //部门物料预算
    private Double groupBalance; //部门物料余额
    private String userName;//姓名
    private AgentRoleType agentRoleType;//角色
    private Double userBalance;//人员物料余额
    private String serviceType;//业务类型
    private String groupStatus;//部门状态是否失效
    private String groupDisableTime;//部门失效时间（状态 disabled=1的updateTime）
    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(schoolTerm);
        result.add(groupName);
        result.add(groupBudget == null ? 0 : groupBudget);
        result.add(groupBalance == null ? 0 : groupBalance);
        result.add(getUserName());
        result.add(agentRoleType == null ? "" : agentRoleType.getRoleName());
        result.add(userBalance == null ? 0 : userBalance);
        result.add(groupStatus);
        result.add(serviceType);
        result.add(groupDisableTime);
        return result;
    }
}
