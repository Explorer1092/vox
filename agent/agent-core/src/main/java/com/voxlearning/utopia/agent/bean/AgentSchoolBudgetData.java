package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentSchoolBudget;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2017-08-10 12:37
 **/

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentSchoolBudgetData {

    private Long schoolId;            // 学校ID
    private String schoolName;
    private Integer month;            // 月份（格式：201709）
    private String permeabilityString;
    private AgentSchoolPermeabilityType permeability;            // 渗透情况： 低渗，中渗，高渗，超高渗
    private Integer sglSubjIncBudget;             // 小单新增预算
    private Integer sglSubjLtBfBudget;            // 小单长回预算
    private Integer sglSubjStBfBudget;            // 小单短回预算
    private Integer engBudget;                    // 英语月活预算
    private Integer mathAnshIncBudget;            // 数扫（扫描数学答题卡）的新增预算
    private Integer mathAnshBfBudget;             // 数扫（扫描数学答题卡）的回流预算

    public static AgentSchoolBudgetData fromAgentSchoolBudget(AgentSchoolBudget agentSchoolBudget){
        AgentSchoolBudgetData agentSchoolBudgetData = new AgentSchoolBudgetData();
        agentSchoolBudgetData.setSchoolId(agentSchoolBudget.getSchoolId());
        agentSchoolBudgetData.setMonth(agentSchoolBudget.getMonth());
        agentSchoolBudgetData.setPermeability(agentSchoolBudget.getPermeability());
        if (null != agentSchoolBudget.getPermeability()){
            agentSchoolBudgetData.setPermeabilityString(agentSchoolBudget.getPermeability().getDesc());
        }
        agentSchoolBudgetData.setSglSubjIncBudget(agentSchoolBudget.getSglSubjIncBudget());
        agentSchoolBudgetData.setSglSubjLtBfBudget(agentSchoolBudget.getSglSubjLtBfBudget());
        agentSchoolBudgetData.setSglSubjStBfBudget(agentSchoolBudget.getSglSubjStBfBudget());
        agentSchoolBudgetData.setEngBudget(agentSchoolBudget.getEngBudget());
        agentSchoolBudgetData.setMathAnshIncBudget(agentSchoolBudget.getMathAnshIncBudget());
        agentSchoolBudgetData.setMathAnshBfBudget(agentSchoolBudget.getMathAnshBfBudget());
        return agentSchoolBudgetData;
    }

    public static List<AgentSchoolBudgetData> fromAgentSchoolBudget(List<AgentSchoolBudget> agentSchoolBudgetList){
        if (CollectionUtils.isEmpty(agentSchoolBudgetList)){
            return Collections.emptyList();
        }
        List<AgentSchoolBudgetData> agentSchoolBudgetDataList = new ArrayList<>();
        agentSchoolBudgetList.forEach(item -> {
            agentSchoolBudgetDataList.add(fromAgentSchoolBudget(item));
        });
        return agentSchoolBudgetDataList;
    }

    public AgentSchoolBudget toAgentSchoolBudget(){
        AgentSchoolBudget budget = new AgentSchoolBudget();
        budget.setPermeability(this.getPermeability());
        budget.setMonth(this.getMonth());
        budget.setSchoolId(this.getSchoolId());
        budget.setSglSubjIncBudget(this.getSglSubjIncBudget());
        budget.setSglSubjLtBfBudget(this.getSglSubjLtBfBudget());
        budget.setSglSubjStBfBudget(this.getSglSubjStBfBudget());
        budget.setEngBudget(this.getEngBudget());
        budget.setMathAnshIncBudget(this.getMathAnshIncBudget());
        budget.setMathAnshBfBudget(this.getMathAnshBfBudget());
        return budget;
    }


}
