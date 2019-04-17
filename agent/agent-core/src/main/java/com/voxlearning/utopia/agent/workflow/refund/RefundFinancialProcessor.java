package com.voxlearning.utopia.agent.workflow.refund;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
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
import java.util.Map;

/**
 * 商品退款财务处理流程
 * Created by Wang Yuechen on 2016/4/6.
 */
@Named
public class RefundFinancialProcessor extends AbstractWorkFlowProcessor {

    @Inject private AgentOrderServiceClient agentOrderServiceClient;
    @Inject private BaseOrderService baseOrderService;

    @ImportService(interfaceClass = CrmRefundService.class)
    private CrmRefundService crmRefundService;

    private static final String ADMIN_REFUND_CALLBACK_URI = "/legacy/afenti/market/withdrawresult.vpage";

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

        Map<String, Object> orderInfo = JsonUtils.convertJsonObjectToMap(agentOrder.getOrderNotes());
        String orderId = SafeConverter.toString(orderInfo.get("orderId"));
        MapMessage message = crmRefundService.agreeCrmRefund(orderId, agentOrder.getCreator());
        if (!message.isSuccess()) {
            throw new UtopiaRuntimeException("退款申请批准失败：" + message.getInfo());
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
        Map<String, Object> orderInfo = JsonUtils.convertJsonObjectToMap(agentOrder.getOrderNotes());
        String orderId = SafeConverter.toString(orderInfo.get("orderId"));
        MapMessage message = crmRefundService.rejectCrmRefund(orderId, agentOrder.getCreator(), context.getProcessNotes());
        if (!message.isSuccess()) {
            throw new UtopiaRuntimeException("退款申请拒绝失败：" + message.getInfo());
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
