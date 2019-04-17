package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBudget;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2018-02-06 18:27
 **/
@Getter
@Setter
public class AgentMaterialBudgetVO implements Serializable,ExportAble {

    private static final long serialVersionUID = -7602909439871956594L;

    private String id;

    /**
     * 预算
     */
    private Double budget;

    /**
     * 余额
     */
    private Double balance;

    /**
     * 预算类型，1：城市预算，2：物料预算
     */
    private Integer budgetType;


    /**
     * 分区ID
     */
    private Long groupId;

    /**
     * 分区名字
     */
    private String groupName;



    //-----------城市预算部分

    /**
     * 城市编码
     */
    private Integer regionCode;

    /**
     * 城市名称
     */
    private String regionName;

    /**
     * 城市级别
     */
    private String regionLevel;


    /**
     * 费用月份
     */
    private Integer month;

    private String cityManager;

    //-----------城市预算部分结束


    //-----------物料费用部分
    private Long userId;

    /**
     * 学期
     */
    private String semester;

    private String userName;

    private String agentRoleType;

    //业务类型
    private String serviceType;

    //部门状态是否失效
    private String groupStatus;

    //部门失效时间（状态 disabled=1的updateTime）
    private String groupDisableTime;
    //------------物料费用部分结束

    //更新类型 （增加/减少）
    private String updateType;
    //备注
    private String content;

    /**
     * 转换预算记录
     */
    public AgentMaterialBudget toAgentMaterialBudget(){
        if (budgetType == 2){
            AgentMaterialBudget agentMaterialBudget = new AgentMaterialBudget();
            agentMaterialBudget.setId(this.getId());
            agentMaterialBudget.setUserId(this.getUserId());
            agentMaterialBudget.setBudget(this.getBudget());
            agentMaterialBudget.setBalance(this.getBalance());
            agentMaterialBudget.setBudgetType(2);
            agentMaterialBudget.setSemester(this.getSemester());
            return agentMaterialBudget;
        }else if (budgetType == 1){
            AgentMaterialBudget agentMaterialBudget = new AgentMaterialBudget();
            agentMaterialBudget.setBudget(this.getBudget());
            agentMaterialBudget.setBalance(this.getBalance());
            agentMaterialBudget.setBudgetType(1);
            agentMaterialBudget.setId(this.getId());
            agentMaterialBudget.setGroupId(this.getGroupId());
            agentMaterialBudget.setGroupName(this.getGroupName());
            agentMaterialBudget.setMonth(this.getMonth());
            agentMaterialBudget.setRegionCode(this.getRegionCode());
            agentMaterialBudget.setRegionName(this.getRegionName());
            return agentMaterialBudget;
        }
        return null;
    }

    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        if (2 == budgetType){
            result.add(semester);
            result.add(groupName);
            if (null != agentRoleType){
                result.add(agentRoleType);
            }else {
                result.add("");
            }
            result.add(userName);
            result.add(budget);
            result.add(balance);
        }else if (1 == budgetType){
            result.add(month);
            result.add(groupName);
            result.add(regionName);
            result.add(regionLevel);
            result.add(cityManager);
            result.add(budget);
            result.add(balance);
            result.add(groupStatus);
            result.add(serviceType);
            result.add(groupDisableTime);
        }
        return result;
    }
}
