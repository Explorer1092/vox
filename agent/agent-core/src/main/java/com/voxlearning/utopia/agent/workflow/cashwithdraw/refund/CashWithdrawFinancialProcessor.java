package com.voxlearning.utopia.agent.workflow.cashwithdraw.refund;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcess;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcessHistory;
import com.voxlearning.utopia.agent.service.common.BaseOrderService;
import com.voxlearning.utopia.agent.workflow.AbstractWorkFlowProcessor;
import com.voxlearning.utopia.agent.workflow.WorkFlowContext;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.service.refund.CrmRefundService;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 余额提现财务处理流程
 * Created by Wang Yuechen on 2016/4/13
 */
@Named
public class CashWithdrawFinancialProcessor extends AbstractWorkFlowProcessor {

    @Inject private BaseOrderService baseOrderService;
    @Inject private AgentOrderServiceClient agentOrderServiceClient;

    @ImportService(interfaceClass = CrmRefundService.class)
    private CrmRefundService crmRefundService;

    private static final String ADMIN_CASH_WITHDRAW_CALLBACK_URI = "/crm/finance/withdrawresult.vpage";

    @Override
    public void agree(WorkFlowContext context) {

        AgentOrder agentOrder = context.getOrder();
        AuthCurrentUser currentUser = context.getCurrentUser();
        AgentOrderProcess agentOrderProcess = baseOrderService.getOrderProcessByOrderId(agentOrder.getId());
        // 防止发起多次操作
        boolean finished = agentOrder.getOrderStatus().equals(AgentOrderStatus.FINISHED.getStatus());
        if (finished) {
            return;
        }

        // 调用CRM端的余额提取的方法
        MapMessage message = crmRefundService.agreeCrmWithdraw(agentOrder.getCreator(), agentOrder.getOrderAmount().doubleValue());
        if (!message.isSuccess()) {
            throw new UtopiaRuntimeException("余额提取批准失败:" + message.getInfo());
        }

        // 记录流转历史
        AgentOrderProcessHistory agentOrderProcessHistory = new AgentOrderProcessHistory();
        agentOrderProcessHistory.setOrderId(agentOrder.getId());
        agentOrderProcessHistory.setProcessNotes(context.getProcessNotes());
        agentOrderProcessHistory.setProcessor(currentUser.getUserId());
        agentOrderProcessHistory.setResult(AgentOrderProcessHistory.RESULT_APPROVED);
        baseOrderService.saveOrderHistory(agentOrderProcessHistory);

        // 更新订单状态
        agentOrder.setLatestProcessorGroup(agentOrderProcess.getTargetGroup());
        agentOrder.setLatestProcessor(currentUser.getUserId());
        agentOrder.setLatestProcessorName(currentUser.getRealName());
        agentOrder.setOrderStatus(AgentOrderStatus.FINISHED.getStatus());
        agentOrderServiceClient.saveOrder(agentOrder);

        // 订单流程结束
        baseOrderService.deleteProcessByOrderId(agentOrder.getId());

    }

    @Override
    public void reject(WorkFlowContext context) {

        AgentOrder agentOrder = context.getOrder();
        boolean rejected = agentOrder.getOrderStatus().equals(AgentOrderStatus.REJECTED.getStatus());
        if (rejected) {
            return;
        }

        AuthCurrentUser currentUser = context.getCurrentUser();
        AgentOrderProcess agentOrderProcess = baseOrderService.getOrderProcessByOrderId(agentOrder.getId());

        // 调用CRM端的退款通过的方法
        // 调用CRM端的余额提取的方法
        MapMessage message = crmRefundService.rejectCrmWithdraw(agentOrder.getCreator(), agentOrder.getOrderAmount().doubleValue(), context.getProcessNotes());
        if (!message.isSuccess()) {
            throw new UtopiaRuntimeException("余额提取拒绝失败:" + message.getInfo());
        }

        //记录流转历史
        AgentOrderProcessHistory agentOrderProcessHistory = new AgentOrderProcessHistory();
        agentOrderProcessHistory.setOrderId(agentOrder.getId());
        agentOrderProcessHistory.setProcessNotes(context.getProcessNotes());
        agentOrderProcessHistory.setProcessor(currentUser.getUserId());
        agentOrderProcessHistory.setResult(AgentOrderProcessHistory.RESULT_REJECTED);
        baseOrderService.saveOrderHistory(agentOrderProcessHistory);

        // 更新订单状态
        agentOrder.setLatestProcessorGroup(agentOrderProcess.getTargetGroup());
        agentOrder.setLatestProcessor(currentUser.getUserId());
        agentOrder.setLatestProcessorName(currentUser.getRealName());
        agentOrder.setOrderStatus(AgentOrderStatus.REJECTED.getStatus());
        agentOrderServiceClient.saveOrder(agentOrder);

        // 订单流程结束
        baseOrderService.deleteProcessByOrderId(agentOrder.getId());

    }
}
