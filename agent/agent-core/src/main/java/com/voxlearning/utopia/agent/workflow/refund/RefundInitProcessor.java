package com.voxlearning.utopia.agent.workflow.refund;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcess;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcessHistory;
import com.voxlearning.utopia.agent.service.common.BaseOrderService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.workflow.AbstractWorkFlowProcessor;
import com.voxlearning.utopia.agent.workflow.WorkFlowContext;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 退款的初始化处理
 * Created by Yuechen.Wang on 2016/4/1.
 */
@Named
public class RefundInitProcessor extends AbstractWorkFlowProcessor {

    @Inject private AgentOrderServiceClient agentOrderServiceClient;
    @Inject private BaseOrderService baseOrderService;
    @Inject private RefundNotifySender refundNotifySender;
    @Inject private BaseOrgService baseOrgService;

    @Override
    public void agree(WorkFlowContext context) {
        AgentOrder agentOrder = context.getOrder();

        //记录流转历史
        AgentOrderProcessHistory agentOrderProcessHistory = new AgentOrderProcessHistory();
        agentOrderProcessHistory.setOrderId(agentOrder.getId());
        agentOrderProcessHistory.setProcessNotes("新建退款申请");
        agentOrderProcessHistory.setProcessor(0L);
        agentOrderProcessHistory.setResult(AgentOrderProcessHistory.RESULT_APPROVED);
        baseOrderService.saveOrderHistory(agentOrderProcessHistory);

        // 生成处理信息
        AgentOrderProcess agentOrderProcess = new AgentOrderProcess();
        agentOrderProcess.setOrderId(agentOrder.getId());
        List<AgentGroupUser> agentGroupUsers = baseOrgService.getGroupUserByRole(AgentRoleType.Finance.getId());
        if (CollectionUtils.isNotEmpty(agentGroupUsers)) {
            agentOrderProcess.setTargetGroup(agentGroupUsers.get(0).getGroupId());
        }
        baseOrderService.saveOrderProcess(agentOrderProcess);

        // 更新订单状态为等待财务确认
        agentOrder.setOrderStatus(AgentOrderStatus.PENDING_FINANCIAL.getStatus());
        agentOrderServiceClient.saveOrder(agentOrder);

        // 通知
        refundNotifySender.initRefundOrderNotify(agentOrder);
    }

}
