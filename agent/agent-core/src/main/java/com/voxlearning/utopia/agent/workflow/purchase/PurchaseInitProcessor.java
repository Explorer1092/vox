package com.voxlearning.utopia.agent.workflow.purchase;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
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
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 材料购买的初始化处理
 * Created by Alex on 14-8-14.
 */
@Named
public class PurchaseInitProcessor extends AbstractWorkFlowProcessor {

    @Inject
    private AgentOrderServiceClient agentOrderServiceClient;
    @Inject
    private BaseOrderService baseOrderService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private PurchaseNotifySender purchaseNotifySender;

    @Override
    public void agree(WorkFlowContext context) {

        AgentOrder agentOrder = context.getOrder();
        AuthCurrentUser agentUser = context.getCurrentUser();

        //记录流转历史
        AgentOrderProcessHistory agentOrderProcessHistory = new AgentOrderProcessHistory();
        agentOrderProcessHistory.setOrderId(agentOrder.getId());
        agentOrderProcessHistory.setProcessNotes("新建订单");
        agentOrderProcessHistory.setProcessor(agentUser.getUserId());
        agentOrderProcessHistory.setResult(AgentOrderProcessHistory.RESULT_APPROVED);

        baseOrderService.saveOrderHistory(agentOrderProcessHistory);

        // 更新订单状态，并将此订单发送到财务审核
        agentOrder.setOrderStatus(AgentOrderStatus.UNCHECKED.getStatus());
        agentOrderServiceClient.saveOrder(agentOrder);



        AgentOrderProcess agentOrderProcess = new AgentOrderProcess();
        agentOrderProcess.setOrderId(agentOrder.getId());

        AgentUser targetUser = baseOrgService.getUserByName("hezhili");
        if(targetUser != null){
            agentOrderProcess.setTargetUser(targetUser.getId());
        }else {
            List<AgentGroupUser> agentGroupUsers = baseOrgService.getGroupUserByRole(AgentRoleType.Country.getId());
            if (CollectionUtils.isNotEmpty(agentGroupUsers)) {
                agentOrderProcess.setTargetGroup(agentGroupUsers.get(0).getGroupId());
            }
        }
        baseOrderService.saveOrderProcess(agentOrderProcess);

        // 通知全国总监
        purchaseNotifySender.sendNotifyToCountryManager(agentOrder);
    }

}
