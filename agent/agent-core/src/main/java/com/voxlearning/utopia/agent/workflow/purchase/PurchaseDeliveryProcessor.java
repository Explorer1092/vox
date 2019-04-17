/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.workflow.purchase;

import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.persist.AgentUserAccountHistoryPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcess;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcessHistory;
import com.voxlearning.utopia.agent.persist.entity.AgentUserAccountHistory;
import com.voxlearning.utopia.agent.service.common.BaseOrderService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.workflow.AbstractWorkFlowProcessor;
import com.voxlearning.utopia.agent.workflow.WorkFlowContext;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

/**
 * 商品购买发货处理
 * Created by Alex on 14-8-14.
 */
@Named
public class PurchaseDeliveryProcessor extends AbstractWorkFlowProcessor {

    @Inject
    private BaseOrderService baseOrderService;
    @Inject
    private AgentOrderServiceClient agentOrderServiceClient;
    @Inject
    private AgentNotifyService agentNotifyService;
    @Inject
    private BaseUserService baseUserService;
    @Inject
    private PurchaseNotifySender purchaseNotifySender;
    @Inject
    private AgentUserAccountHistoryPersistence agentUserAccountHistoryPersistence;

    @Override
    public void agree(WorkFlowContext context) {

        AgentOrder agentOrder = context.getOrder();
        boolean finished = agentOrder.getOrderStatus().equals(AgentOrderStatus.FINISHED.getStatus());

        //如果通过了点数抵现的订单，需要把点数扣掉,finished防止用户开多个页面，点了多次通过，重复扣点数
        if (agentOrder.getPointChargeAmount() > 0 && !finished) {
            AgentUser agentUser = baseUserService.getById(agentOrder.getCreator());
            float pointBefore = agentUser.getPointAmount();
            float chargedAmount = pointBefore - agentOrder.getPointChargeAmount();
            if (chargedAmount < 0) {
                throw new RuntimeException("余额不足以抵现!");
            }
            agentUser.setPointAmount(chargedAmount);
            baseUserService.updateAgentUser(agentUser);

            //加入账户历史明细
            AgentUserAccountHistory agentUserAccountHistory = new AgentUserAccountHistory();
            agentUserAccountHistory.setUserId(agentUser.getId());
            agentUserAccountHistory.setCashBefore(agentUser.getCashAmount());
            agentUserAccountHistory.setCashAmount(0f);
            agentUserAccountHistory.setCashAfter(agentUser.getCashAmount());
            agentUserAccountHistory.setPointBefore(pointBefore);
            agentUserAccountHistory.setPointAmount(agentOrder.getPointChargeAmount());
            agentUserAccountHistory.setPointAfter(chargedAmount);
            agentUserAccountHistory.setOrderId(agentOrder.getId());
            agentUserAccountHistory.setComments(AgentOrderType.of(agentOrder.getOrderType()).getDesc());
            agentUserAccountHistoryPersistence.insert(agentUserAccountHistory);
        }


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
        agentOrder.setOrderStatus(AgentOrderStatus.FINISHED.getStatus());
        agentOrderServiceClient.saveOrder(agentOrder);

        // 结束处理流程
        baseOrderService.deleteProcessByOrderId(agentOrder.getId());


        // 发送通知到订单发起人
        Long creator = agentOrder.getCreator();
        String notifyContent = "您的订单(" + agentOrder.getId() + ")已发货，请注意查收！发货附加信息:" + context.getProcessNotes();
        agentNotifyService.sendNotify(AgentNotifyType.ORDER_DELIVERY_NOTICE.getType(),"物料发货", notifyContent,
                Collections.singletonList(creator),null);
    }

    @Override
    public void reject(WorkFlowContext context) {
        AgentOrder agentOrder = context.getOrder();
        boolean rejected = agentOrder.getOrderStatus().equals(AgentOrderStatus.REJECTED.getStatus());
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

        // 订单流程结束
        baseOrderService.deleteProcessByOrderId(agentOrder.getId());


        //如果拒绝了点数抵现的订单，需要把点数返还给代理账户,rejected防止用户开多个页面，点了多次拒绝，重复加点数
        if (agentOrder.getPointChargeAmount() > 0 && !rejected) {
            AgentUser agentUser = baseUserService.getById(agentOrder.getCreator());
            agentUser.setUsablePointAmount(agentOrder.getPointChargeAmount() + agentUser.getUsablePointAmount());
            baseUserService.updateAgentUser(agentUser);
        }

        //发通知给发起人
        purchaseNotifySender.sendRejectOrderNotify(agentOrder, context.getProcessNotes());
    }

}
