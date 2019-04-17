package com.voxlearning.utopia.agent.workflow.purchase;

import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.persist.AgentProductPersistence;
import com.voxlearning.utopia.agent.persist.entity.*;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 购买流程发送通知
 * Created by Shuai Huan on 2014/8/15.
 */
@Named
public class PurchaseNotifySender {

    @Inject
    private AgentProductPersistence agentProductPersistence;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private BaseUserService baseUserService;
    @Inject
    private AgentNotifyService agentNotifyService;

    //大区总监，财务拒绝订单时，发送通知给流程creator
        public void sendRejectOrderNotify(AgentOrder agentOrder, String processNotes) {

            StringBuilder notifyContent = new StringBuilder();
            notifyContent.append("您购买的商品:");
            List<AgentOrderProduct> agentOrderProductList = agentOrder.getOrderProductList();
            Long groupId = agentOrder.getLatestProcessorGroup();
            for (AgentOrderProduct agentOrderProduct : agentOrderProductList) {
                AgentProduct agentProduct = agentProductPersistence.load(agentOrderProduct.getProductId());
                notifyContent.append(agentProduct.getProductName()).append(" 数量:").append(agentOrderProduct.getProductQuantity()).append(",");
            }
            notifyContent.deleteCharAt(notifyContent.length() - 1);
            AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
            if (agentGroup != null) {
                notifyContent.append(" 的订单被" + agentGroup.getGroupName() + "拒绝，理由是:" + (processNotes == null? "" : processNotes));
            } else {
                notifyContent.append(" 的订单被拒绝，理由是:" + (processNotes == null? "" : processNotes));
            }

            agentNotifyService.sendNotify(AgentNotifyType.ORDER_REJECT_NOTICE.getType(), notifyContent.toString(), Collections.singletonList(agentOrder.getCreator()));
    }


    //代理购买流程发起时，发通知给财务，确认收款
    public void sendNotifyToCountryManager(AgentOrder agentOrder) {
        Long creator = agentOrder.getCreator();
        List<Long> groupList = baseOrgService.getGroupListByRole(creator, AgentGroupRoleType.Region);
        Set<Long> receiverList = groupList.stream().map(baseOrgService::getGroupManager).filter(p -> p != null).collect(Collectors.toSet());
        AgentUser user = baseUserService.getById(creator);
        StringBuilder notifyContent = new StringBuilder();
        notifyContent.append("市场人员[").append(user.getAccountName()).append(",").append(user.getRealName()).append("]");
        notifyContent.append("提交了商品购买申请，购买内容:");
        List<AgentOrderProduct> orderProductList = agentOrder.getOrderProductList();
        for (AgentOrderProduct orderProduct : orderProductList) {
            AgentProduct product = agentProductPersistence.load(orderProduct.getProductId());
            notifyContent.append(product.getProductName()).append(" 数量:").append(orderProduct.getProductQuantity()).append(",");
        }
        notifyContent.deleteCharAt(notifyContent.length() - 1);
        agentNotifyService.sendNotify(AgentNotifyType.ORDER_NOTICE.getType(), notifyContent.toString(), receiverList);
    }
}
