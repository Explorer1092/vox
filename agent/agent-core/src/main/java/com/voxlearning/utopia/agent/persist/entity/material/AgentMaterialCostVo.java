/**
 * Author:   xianlong.zhang
 * Date:     2018/10/30 18:19
 * Description: 物料费用更新记录
 * History:
 */
package com.voxlearning.utopia.agent.persist.entity.material;

import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import lombok.Getter;
import lombok.Setter;


public class AgentMaterialCostVo extends AgentMaterialCost{
    @Getter
    @Setter
    private String updateType;//更新类型
    @Getter
    @Setter
    private String comment; //备注

    public static AgentMaterialCostVo fromAgentMaterialCost(AgentMaterialCost agentMaterialCost) {
        if (null == agentMaterialCost) {
            return null;
        }
        AgentMaterialCostVo vo = new AgentMaterialCostVo();
        try {
            BeanUtils.copyProperties(vo, agentMaterialCost);
        } catch (Exception e) {
            return null;
        }
        return vo;
    }

    public AgentMaterialCost toAgentMaterialCost() {
        AgentMaterialCost agentMaterialCost = new AgentMaterialCost();
        try {
            BeanUtils.copyProperties(agentMaterialCost, this);
        } catch (Exception e) {
            return null;
        }
        return agentMaterialCost;
    }
}
