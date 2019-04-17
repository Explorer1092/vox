package com.voxlearning.utopia.agent.workflow.cashdeposit;

import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcess;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcessHistory;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.agent.service.common.BaseOrderService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.workflow.AbstractWorkFlowProcessor;
import com.voxlearning.utopia.agent.workflow.WorkFlowContext;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

/**
 * Created by Alex on 14-8-8.
 */
@Named
public class CashDepositPaymentRecvProcessor extends AbstractWorkFlowProcessor {

    @Inject
    private AgentOrderServiceClient agentOrderServiceClient;
    @Inject
    private BaseOrderService baseOrderService;
    @Inject
    private BaseUserService baseUserService;
    @Inject
    private AgentNotifyService agentNotifyService;

    @Override
    public void agree(WorkFlowContext context) {

        AgentOrder agentOrder = context.getOrder();
        AuthCurrentUser currentUser = context.getCurrentUser();
        AgentOrderProcess agentOrderProcess = baseOrderService.getOrderProcessByOrderId(agentOrder.getId());

        //更新订单状态
        agentOrder.setLatestProcessorGroup(agentOrderProcess.getTargetGroup());
        agentOrder.setLatestProcessor(currentUser.getUserId());
        agentOrder.setLatestProcessorName(currentUser.getRealName());
        agentOrder.setOrderStatus(AgentOrderStatus.FINISHED.getStatus());
        agentOrderServiceClient.saveOrder(agentOrder);

        baseOrderService.deleteProcessByOrderId(agentOrder.getId());

        //记录流转历史
        AgentOrderProcessHistory agentOrderProcessHistory = new AgentOrderProcessHistory();
        agentOrderProcessHistory.setOrderId(agentOrder.getId());
        agentOrderProcessHistory.setProcessNotes(context.getProcessNotes());
        agentOrderProcessHistory.setProcessor(currentUser.getUserId());
        agentOrderProcessHistory.setResult(AgentOrderProcessHistory.RESULT_APPROVED);
        baseOrderService.saveOrderHistory(agentOrderProcessHistory);

        String accountName = agentOrder.getOrderNotes();
        if (accountName.indexOf("#") > 0) {
            accountName = accountName.substring(0, accountName.indexOf("#"));
        }

        AgentUser agentUser = baseUserService.getByAccountName(accountName);
        if (agentUser != null) {
            if (agentUser.getCashDepositReceived() == null || !agentUser.getCashDepositReceived()) {
                agentUser.setCashDepositReceived(true);
            } else {
                agentUser.setCashDeposit(agentUser.getCashDeposit() + agentOrder.getOrderAmount().intValue());
            }
            baseUserService.updateAgentUser(agentUser);
        }

        //财务确认收款后发通知给发起人
        sendNotify(agentOrder.getOrderNotes(), agentOrder.getCreator());
    }

    private void sendNotify(String userName, Long receiverId) {
        String content = userName + "的保证金已收到，请继续后续流程。";
        agentNotifyService.sendNotify(AgentNotifyType.DEPOSIT_CONFIRM.getType(), content, Collections.singletonList(receiverId));
    }

}
