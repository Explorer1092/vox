package com.voxlearning.utopia.agent.workflow.purchase;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcess;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcessHistory;
import com.voxlearning.utopia.agent.persist.entity.AgentProduct;
import com.voxlearning.utopia.agent.service.common.BaseOrderService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.sysconfig.ProductConfigService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.workflow.AbstractWorkFlowProcessor;
import com.voxlearning.utopia.agent.workflow.WorkFlowContext;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 商品购买财务处理流程
 * Created by Alex on 14-8-14.
 */
@Named
public class PurchasePaymentRecvProcessor extends AbstractWorkFlowProcessor {


    @Inject
    private BaseOrderService baseOrderService;
    @Inject
    private AgentOrderServiceClient agentOrderServiceClient;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private BaseUserService baseUserService;
    @Inject
    private PurchaseNotifySender purchaseNotifySender;
    @Inject
    ProductConfigService productConfigService;
    @Inject private SmsServiceClient smsServiceClient;

    @Override
    public void agree(WorkFlowContext context) {

        AgentOrder agentOrder = context.getOrder();
        AuthCurrentUser currentUser = context.getCurrentUser();
        AgentOrderProcess agentOrderProcess = baseOrderService.getOrderProcessByOrderId(agentOrder.getId());

        //记录流转历史
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
        agentOrder.setOrderStatus(AgentOrderStatus.APPROVED.getStatus());
        agentOrderServiceClient.saveOrder(agentOrder);

        // 更新用户账户余额
        Long creator = agentOrder.getCreator();
        if(creator != null && creator != 0){
            AgentUser agentUser = baseUserService.getById(agentOrder.getCreator());
            if(agentUser != null){
                agentUser.setCashAmount(MathUtils.floatSub(agentUser.getCashAmount(), agentOrder.getOrderAmount()));
                baseUserService.updateAgentUser(agentUser);
            }
        }

        // 将订单转给物料中心
        baseOrderService.deleteProcessByOrderId(agentOrder.getId());
        List<AgentGroupUser> agentGroupUsers = baseOrgService.getGroupUserByRole(AgentRoleType.MATERIAL.getId());
        AgentOrderProcess orderProcess = new AgentOrderProcess();
        orderProcess.setOrderId(agentOrder.getId());
        if(CollectionUtils.isNotEmpty(agentGroupUsers)){
            orderProcess.setTargetGroup(agentGroupUsers.get(0).getGroupId());
        }
        baseOrderService.saveOrderProcess(orderProcess);
    }

    @Override
    public void reject(WorkFlowContext context) {

        AgentOrder agentOrder = context.getOrder();
        boolean rejected = agentOrder.getOrderStatus().equals(AgentOrderStatus.REJECTED.getStatus());
        if(rejected){ // 已经有人拒绝了， 属重复操作，直接返回
            return;
        }
        AuthCurrentUser currentUser = context.getCurrentUser();
        AgentOrderProcess agentOrderProcess = baseOrderService.getOrderProcessByOrderId(agentOrder.getId());

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

        // 更新库存
        List<AgentOrderProduct> orderProductList = agentOrder.getOrderProductList();
        if (CollectionUtils.isNotEmpty(orderProductList)) {

            List<AgentGroup> groupList = baseOrgService.getUserGroups(agentOrder.getCreator());
            String groupName = "";
            if(CollectionUtils.isNotEmpty(groupList)){
                groupName = groupList.get(0).getGroupName();
            }
            AgentUser user = baseOrgService.getUser(agentOrder.getCreator());
            String quantityChangeDesc = "【驳回订单】订单号：" + agentOrder.getId() + "(" + (StringUtils.isBlank(groupName)? "" : groupName + " - ") + (user == null? "" : user.getRealName()) + ")";

            orderProductList.stream().forEach(p -> {
                AgentProduct product = productConfigService.getById(p.getProductId());
                Integer preQuantity = product.getInventoryQuantity() == null ? 0: product.getInventoryQuantity();
                product.setInventoryQuantity(product.getInventoryQuantity() + p.getProductQuantity());
                productConfigService.updateProduct(product);

                // 添加库存变更记录
                productConfigService.addAgentProductInventoryRecord(context.getCurrentUser().getUserId(), product.getId(), preQuantity, product.getInventoryQuantity(), p.getProductQuantity(), quantityChangeDesc);
            });
        }

        // 更新用户账户可用余额
        Long creator = agentOrder.getCreator();
        if (creator != null && creator != 0) {
            AgentUser agentUser = baseUserService.getById(agentOrder.getCreator());
            if (agentUser != null) {
                agentUser.setUsableCashAmount(MathUtils.floatAdd(agentUser.getUsableCashAmount(), agentOrder.getOrderAmount()));
                baseUserService.updateAgentUser(agentUser);

                if(StringUtils.isNotBlank(agentUser.getTel())){
                    SmsMessage smsMessage = new SmsMessage();
                    smsMessage.setMobile(agentUser.getTel());
                    smsMessage.setType(SmsType.MARKET_ADD_AGENT_USER.name());
                    DateUtils.dateToString(agentOrder.getCreateDatetime(), "MM月dd日");
                    smsMessage.setSmsContent("您于" + DateUtils.dateToString(agentOrder.getCreateDatetime(), "MM月dd日") +"发起的物料申请由于「" + context.getProcessNotes() +"」的原因被驳回，订单号为" + agentOrder.getId()+ "。");
                    smsServiceClient.getSmsService().sendSms(smsMessage);
                }
            }
        }

        // 订单流程结束
        baseOrderService.deleteProcessByOrderId(agentOrder.getId());

        //发通知给发起人
        purchaseNotifySender.sendRejectOrderNotify(agentOrder, context.getProcessNotes());

    }

}
