package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceGoal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * AgentPerformanceGoalVO
 *
 * @author chunlin.yu
 * @create 2017-10-26 16:17
 **/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentPerformanceGoalVO  implements ExportAble {

    private String id;

    /** 业绩目标类型 */
    private String agentPerformanceGoalTypeDesc;

    /** 大区ID */
    private Long regionGroupId;
    private String regionGroupName;

    /** 分区ID */
    private Long subRegionGroupId;
    private String subRegionGroupName;

    /** 专员ID */
    private Long businessDeveloperId;
    private String businessDeveloperName;

    /** 业绩目标月份（格式：201709）*/
    private Integer month;

    /** 小单新增目标 */
    private Integer sglSubjIncGoal;

    /** 小单长回目标 */
    private Integer sglSubjLtBfGoal;

    /** 小单短回目标 */
    private Integer sglSubjStBfGoal;

    /** 业绩目标是否被确认 */
    private Boolean confirm;

    public AgentPerformanceGoal toAgentPerformanceGoal(){
        AgentPerformanceGoal agentPerformanceGoal = new AgentPerformanceGoal();
        agentPerformanceGoal.setId(this.getId());
        agentPerformanceGoal.setAgentPerformanceGoalType(AgentPerformanceGoalType.of(this.getAgentPerformanceGoalTypeDesc()));
        agentPerformanceGoal.setUserId(this.getBusinessDeveloperId());
        agentPerformanceGoal.setMonth(this.getMonth());
        agentPerformanceGoal.setRegionGroupId(this.getRegionGroupId());
        agentPerformanceGoal.setSubRegionGroupId(this.getSubRegionGroupId());
        agentPerformanceGoal.setSglSubjIncGoal(this.getSglSubjIncGoal());
        agentPerformanceGoal.setSglSubjLtBfGoal(this.getSglSubjLtBfGoal());
        agentPerformanceGoal.setSglSubjStBfGoal(this.getSglSubjStBfGoal());
        return agentPerformanceGoal;
    }

    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(this.getMonth());
        result.add(this.getAgentPerformanceGoalTypeDesc());
        result.add(this.getRegionGroupName());
        result.add(this.getSubRegionGroupName());
        result.add(this.getBusinessDeveloperName());
        result.add(this.getSglSubjIncGoal());
        result.add(this.getSglSubjLtBfGoal());
        result.add(this.getSglSubjStBfGoal());
        result.add(this.getSglSubjIncGoal() + this.getSglSubjLtBfGoal() + this.getSglSubjStBfGoal());
        return result;
    }
}
