package com.voxlearning.utopia.agent.workflow.cashdeposit;

import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcess;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcessHistory;
import com.voxlearning.utopia.agent.service.common.BaseGroupService;
import com.voxlearning.utopia.agent.service.common.BaseOrderService;
import com.voxlearning.utopia.agent.workflow.AbstractWorkFlowProcessor;
import com.voxlearning.utopia.agent.workflow.WorkFlowContext;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by Alex on 14-8-8.
 */
@Named
public class CashDepositInitProcessor extends AbstractWorkFlowProcessor {

    @Inject
    private AgentOrderServiceClient agentOrderServiceClient;
    @Inject
    private BaseGroupService baseGroupService;
    @Inject
    private BaseOrderService baseOrderService;

    @Override
    public void agree(WorkFlowContext context) {

        AgentOrder agentOrder = context.getOrder();
        AuthCurrentUser agentUser = context.getCurrentUser();

        agentOrder.setOrderStatus(AgentOrderStatus.PENDING_FINANCIAL.getStatus());
        agentOrderServiceClient.saveOrder(agentOrder);
        List<AgentGroup> agentGroups = baseGroupService.getAgentGroupByRoleId(AgentRoleType.Finance.getId());
        AgentOrderProcess agentOrderProcess = new AgentOrderProcess();
        agentOrderProcess.setOrderId(agentOrder.getId());
        if (agentGroups != null && agentGroups.size() > 0) {
            agentOrderProcess.setTargetGroup(agentGroups.get(0).getId());
        }
        baseOrderService.saveOrderProcess(agentOrderProcess);

        //记录流转历史
        AgentOrderProcessHistory agentOrderProcessHistory = new AgentOrderProcessHistory();
        agentOrderProcessHistory.setOrderId(agentOrder.getId());
        agentOrderProcessHistory.setProcessNotes(context.getProcessNotes());
        agentOrderProcessHistory.setProcessor(agentUser.getUserId());
        agentOrderProcessHistory.setResult(AgentOrderProcessHistory.RESULT_APPROVED);

        baseOrderService.saveOrderHistory(agentOrderProcessHistory);
    }

}
