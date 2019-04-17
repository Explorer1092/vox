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

package com.voxlearning.utopia.agent.service.task;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.persist.AgentOrderProcessHistoryPersistence;
import com.voxlearning.utopia.agent.persist.AgentOrderProcessPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcess;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcessHistory;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrderService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.log.AsyncLogService;
import com.voxlearning.utopia.agent.workflow.WorkFlowContext;
import com.voxlearning.utopia.agent.workflow.WorkFlowEngine;
import com.voxlearning.utopia.agent.workflow.refund.RefundProcessorFactory;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentGatewayManager;
import com.voxlearning.utopia.payment.RefundRequest;
import com.voxlearning.utopia.payment.WechatAbstractPaymentGateway;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.service.refund.CrmRefundService;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrderLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.constants.RefundHistoryStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderRefundHistory;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/15.
 */
@Named
public class TaskService extends AbstractAgentService {
    @Inject private AgentOrderProcessPersistence agentOrderProcessPersistence;
    @Inject private AgentOrderProcessHistoryPersistence agentOrderProcessHistoryPersistence;
    @Inject private BaseOrgService baseOrgService;
    @Inject private BaseUserService baseUserService;
    @Inject private BaseOrderService baseOrderService;
    @Inject private WorkFlowEngine workFlowEngine;
    @Inject private AgentOrderLoaderClient agentOrderLoaderClient;
    @Inject private AgentOrderServiceClient agentOrderServiceClient;
    @Inject private PaymentGatewayManager paymentGatewayManager;
    @Inject private AsyncLogService asyncLogService;
    @Inject private UserOrderServiceClient userOrderServiceClient;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;

    @ImportService(interfaceClass = CrmRefundService.class)
    private CrmRefundService crmRefundService;

    private Map<Integer, AgentOrderType> orderTypeMap = AgentOrderType.toMap();

    /**
     * 获取指派给user以及user所在群组的所有任务
     *
     * @param userId
     * @return
     */
    public List<Map<String, Object>> getAssignments(Long userId) {
        List<Map<String, Object>> userAssignments = getAssignmentsByType(userId, 0);
        List<AgentGroup> agentGroups = baseOrgService.getUserGroups(userId);
        for (AgentGroup agentGroup : agentGroups) {
            userAssignments.addAll(getAssignmentsByType(agentGroup.getId(), 1));
        }
        return userAssignments;
    }

    //这里需要详细说明一下：根据不同的订单类型，走的流程是不一样的。
    //提现和充值订单，通过之后，打给creator
    //购买充值卡订单，通过之后，打给全国的group
    //购买材料订单，通过之后，打给大区group？省group？
    public void approveOrder(Long taskId, Integer orderType, AuthCurrentUser processor, String comment) {
        if (taskId == null || taskId <= 0 || orderType == null || orderTypeMap.get(orderType) == null) {
            return;
        }
        AgentOrderProcess agentOrderProcess = agentOrderProcessPersistence.loadFromDatabase(taskId);
        if (agentOrderProcess == null) return;
        AgentOrder agentOrder = agentOrderLoaderClient.loadAgentOrderById(agentOrderProcess.getOrderId());
        WorkFlowContext context = new WorkFlowContext(agentOrder, processor);
        context.setProcessNotes(comment);
        workFlowEngine.getWorkFlowProcessorFactory(context).getProcessor(context).agree(context);

    }

    public void rejectOrder(Long taskId, AuthCurrentUser processor, String comment) {
        if (taskId == null || taskId <= 0) {
            return;
        }
        AgentOrderProcess agentOrderProcess = agentOrderProcessPersistence.loadFromDatabase(taskId);
        if (agentOrderProcess == null) return;
        AgentOrder agentOrder = baseOrderService.getOrderById(agentOrderProcess.getOrderId());
        if (AgentOrderStatus.of(agentOrder.getOrderStatus()) == AgentOrderStatus.REJECTED) {
            return;
        }
        WorkFlowContext context = new WorkFlowContext(agentOrder, processor);
        context.setProcessNotes(comment);
        workFlowEngine.getWorkFlowProcessorFactory(context).getProcessor(context).reject(context);

    }


    /**
     * 提出订单流程的用户在订单流程结束时，确认订单
     *
     * @param taskId
     */
    public void confirmOrder(Long taskId, AuthCurrentUser currentUser) {
        if (taskId == null || taskId <= 0) {
            return;
        }
        AgentOrderProcess agentOrderProcess = baseOrderService.getOrderProcessById(taskId);
        if (agentOrderProcess == null)
            throw new RuntimeException("此流程不存在,请刷新页面。");
        AgentOrder agentOrder = baseOrderService.getOrderById(agentOrderProcess.getOrderId());

        WorkFlowContext workFlowContext = new WorkFlowContext(agentOrder, currentUser);
        workFlowEngine.getWorkFlowProcessorFactory(workFlowContext).getProcessor(workFlowContext).agree(workFlowContext);
    }


    public List<Map<String, Object>> loadOrderHistory(Long orderId) {
        if (orderId == null || orderId < 0)
            return Collections.emptyList();
        List<Map<String, Object>> orderHistories = new ArrayList<>();
        List<AgentOrderProcessHistory> histories = agentOrderProcessHistoryPersistence.findByOrderId(orderId);
        if (histories != null) {
            for (AgentOrderProcessHistory history : histories) {
                Map<String, Object> map = new HashMap<>();
                AgentUser agentUser = baseUserService.getById(history.getProcessor());
                if (agentUser != null)
                    map.put("processor", agentUser.getRealName());
                if (AgentOrderProcessHistory.RESULT_APPROVED == history.getResult())
                    map.put("result", "通过");
                if (AgentOrderProcessHistory.RESULT_REJECTED == history.getResult())
                    map.put("result", "拒绝");
                map.put("notes", history.getProcessNotes());
                map.put("time", DateUtils.dateToString(history.getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
                orderHistories.add(map);
            }
        }
        return orderHistories;
    }

    /**
     * 私有方法
     * 根据type不同，获取不同assignment
     * type=0 按target_user查
     * type=1 按target_group查
     *
     * @param targetId
     * @param type
     * @return
     */
    private List<Map<String, Object>> getAssignmentsByType(Long targetId, Integer type) {

        List<Map<String, Object>> result = new ArrayList<>();
        List<AgentOrderProcess> processes = new ArrayList<>();
        if (type == 0) {//按用户查
            processes = agentOrderProcessPersistence.findByTargetUser(targetId);
        } else if (type == 1) {
            processes = agentOrderProcessPersistence.findByTargetGroup(targetId);
        }

        if (CollectionUtils.isEmpty(processes)) return new ArrayList<>();
        // FIXME 循环去从数据区load会降低效率吧，统一Load出来处理吧 By Wyc 2016-05-27
        Set<Long> orderId = processes.stream().map(AgentOrderProcess::getOrderId).collect(Collectors.toSet());
        Map<Long, AgentOrder> agentOrderMap = agentOrderLoaderClient.loadAgentOrderByIds(orderId);
        for (AgentOrderProcess process : processes) {
            AgentOrder order = agentOrderMap.get(process.getOrderId());
            if (order == null || order.getOrderStatus() == 99) {
                continue;
            }
            AgentOrderType orderType = AgentOrderType.of(order.getOrderType());
            if (orderType != null && (AgentOrderType.BUY_MATERIAL == orderType || AgentOrderType.DEPOSIT == orderType)) {
                continue;
            }

            Map<String, Object> orderMap = baseOrderService.generateOrderMap(order);
            orderMap.put("id", process.getId());
            result.add(orderMap);
        }
        return result;
    }

    public List<Map<String, Object>> getMyProceedOrderList(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<AgentOrderProcessHistory> hisList = agentOrderProcessHistoryPersistence.findByProcessor(userId);
        if (hisList != null && hisList.size() > 0) {
            // FIXME 循环去从数据区load会降低效率吧，统一Load出来处理吧 By Wyc 2016-05-27
            // 展示前1000 条
            Set<Long> orderId = hisList.stream().limit(1000).map(AgentOrderProcessHistory::getOrderId).collect(Collectors.toSet());
            Map<Long, AgentOrder> agentOrderMap = agentOrderLoaderClient.loadAgentOrderByIds(orderId);
            for (AgentOrderProcessHistory processHistory : hisList) {
                AgentOrder order = agentOrderMap.get(processHistory.getOrderId());
                if (order == null) {
                    continue;
                }
                Map<String, Object> orderMap = baseOrderService.generateOrderMap(order);
                orderMap.put("createDatetime", processHistory.getCreateDatetime());
                orderMap.put("processNote", processHistory.getProcessNotes());
                orderMap.put("result", processHistory.getResult());
                result.add(orderMap);
            }
        }
        return result;
    }

    public MapMessage createRefundOrder(AgentOrder agentOrder, String creator, String latestProcessor) {
        try {
            // 初始化一个Agent订单
            agentOrder.setCreatorGroup(RefundProcessorFactory.getCrmAdminGroupId());
            agentOrder.setOrderType(AgentOrderType.REFUND.getType());
            agentOrder.setOrderStatus(AgentOrderStatus.INIT.getStatus());
            agentOrder.setPointChargeAmount(0F);
            agentOrder.setLatestProcessorGroup(RefundProcessorFactory.getCrmAdminGroupId());

            Long orderId = agentOrderServiceClient.saveOrder(agentOrder);
            baseOrderService.updateCrmUserInfo(orderId, creator, latestProcessor);
            // 生成一个工作流Context
            agentOrder = baseOrderService.getOrderById(orderId);
            WorkFlowContext context = new WorkFlowContext(agentOrder, null);

            // 创建一个退货订单
            workFlowEngine.getWorkFlowProcessorFactory(context).getProcessor(context).agree(context);

            // 反馈一些信息
            return MapMessage.successMessage().add("orderId", orderId);
        } catch (Exception ex) {
            logger.error("Failed to init a refund order : {}", ex.getMessage(), ex);
            return MapMessage.errorMessage("Failed to init a refund order : {}", ex.getMessage());
        }
    }

    public MapMessage createCashWithdraw(AgentOrder agentOrder, String creator, String latestProcessor) {
        try {
            // 初始化一个Agent订单
            agentOrder.setCreatorGroup(RefundProcessorFactory.getCrmAdminGroupId());
            agentOrder.setOrderType(AgentOrderType.CASH_WITHDRAW.getType());
            agentOrder.setOrderStatus(AgentOrderStatus.INIT.getStatus());
            agentOrder.setPointChargeAmount(0F);
            agentOrder.setLatestProcessorGroup(RefundProcessorFactory.getCrmAdminGroupId());

            Long orderId = agentOrderServiceClient.saveOrder(agentOrder);
            baseOrderService.updateCrmUserInfo(orderId, creator, latestProcessor);
            // 生成一个工作流Context
            agentOrder = baseOrderService.getOrderById(orderId);
            WorkFlowContext context = new WorkFlowContext(agentOrder, null);

            // 创建一个余额额提现申请
            workFlowEngine.getWorkFlowProcessorFactory(context).getProcessor(context).agree(context);

            // 反馈一些信息
            return MapMessage.successMessage().add("orderId", orderId);
        } catch (Exception ex) {
            logger.error("Failed to init a refund order : {}", ex.getMessage(), ex);
            return MapMessage.errorMessage("Failed to init a refund order : {}", ex.getMessage());
        }
    }

    public MapMessage loadCrmFinanceFlow(Long agentOrderId) {
        try {
//            final String crmFinanceUrl = "/crm/finance/userpaymenthistory.vpage";

            AgentOrder agentOrder = baseOrderService.getOrderById(agentOrderId);
            if (agentOrder == null) {
                return MapMessage.errorMessage("该申请已经被删除");
            }
            String transactionIds;
            if (agentOrder.getOrderType().equals(AgentOrderType.REFUND.getType())) {
                Map<String, Object> crmOrderInfo = JsonUtils.convertJsonObjectToMap(agentOrder.getOrderNotes());
                transactionIds = SafeConverter.toString(crmOrderInfo.get("transactionId"));
            } else if (agentOrder.getOrderType().equals(AgentOrderType.CASH_WITHDRAW.getType())) {
                transactionIds = agentOrder.getOrderNotes();
            } else {
                return MapMessage.errorMessage("无法获得支付流水号");
            }
            return crmRefundService.loadCrmFinanceFlow(agentOrder.getCreator(), transactionIds);
        } catch (Exception ex) {
            logger.error("Failed to load CRM finance flow : {}", ex.getMessage(), ex);
            return MapMessage.errorMessage("获取CRM财务流水失败 : {}", ex.getMessage());
        }
    }


    public MapMessage batchRefundUserOrder(AuthCurrentUser currentUser, String orderIds, String comment) {
        List<Map<String, Object>> resultInfos = new ArrayList<>();
        String[] orderIdsArray = StringUtils.split(orderIds, ",");
        List<Long> processIds = new ArrayList<>();
        for (String id : orderIdsArray) {
            processIds.add(SafeConverter.toLong(id));
        }
        if (CollectionUtils.isEmpty(processIds)) {
            return MapMessage.errorMessage("无效的数据，请刷新页面重试");
        }

        // 获取订单信息
        List<AgentOrderProcess> processes = agentOrderProcessPersistence.loadFromDatabase(processIds);
        if (CollectionUtils.isEmpty(processes)) {
            return MapMessage.errorMessage("无效的数据，请刷新页面重试");
        }

        // 组装退款需要的参数
        List<RefundRequest> refundRequestList = new ArrayList<>();
        for (AgentOrderProcess orderProcess : processes) {
            AgentOrder agentOrder = agentOrderLoaderClient.loadAgentOrderById(orderProcess.getOrderId());
            if (agentOrder == null) {
                continue;
            }
            if (StringUtils.isBlank(agentOrder.getOrderNotes())) {
                continue;
            }
            Map<String, Object> orderNotes = JsonUtils.fromJson(agentOrder.getOrderNotes());
            if (orderNotes == null) {
                continue;
            }
            String payMethod = SafeConverter.toString(orderNotes.get("payMethod"));
            if (StringUtils.isBlank(payMethod) || (!payMethod.contains("alipay") && !payMethod.contains("wechatpay"))) {
                continue;
            }
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setPayMethod(payMethod);
            refundRequest.setAgentProcessId(orderProcess.getId());
            refundRequest.setUserId(agentOrder.getCreator());
            refundRequest.setOrderId(SafeConverter.toString(orderNotes.get("orderId")));
            refundRequest.setTotalFee(SafeConverter.toDouble(orderNotes.get("amount")));

            // 精度处理
            refundRequest.setRefundFee(new BigDecimal(agentOrder.getOrderAmount()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            List<String> transactionIds = JsonUtils.fromJsonToList(SafeConverter.toString(orderNotes.get("transactionId")), String.class);
            if (CollectionUtils.isNotEmpty(transactionIds) && transactionIds.size() == 1) {
                refundRequest.setTransactionId(transactionIds.get(0));
            }
            refundRequestList.add(refundRequest);
        }
        if (CollectionUtils.isEmpty(refundRequestList)) {
            return MapMessage.errorMessage("该功能只支持微信或者支付宝退款，请检查后重试");
        }
        // 如果是一个单子的退款 直接执行
        if (refundRequestList.size() == 1) {
            RefundRequest refundRequest = refundRequestList.get(0);
            PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(refundRequest.getPayMethod());
            if (paymentGateway == null) {
                return MapMessage.errorMessage("不支持的支付方式");
            }
            // 判断微信支付宝
            if (refundRequest.getPayMethod().contains("wechat")) {
                resultInfos.add(wechatRefund(refundRequest, currentUser, comment));
                return MapMessage.successMessage().add("resultInfos", resultInfos);
            } else if (refundRequest.getPayMethod().contains("alipay")) {
                // 支付宝退款  首先拼装成支付宝需要的格式
                refundRequest.setBatchNum("1");
                String refundDetail = refundRequest.getTransactionId() + "^" + refundRequest.getRefundFee() + "^协商退款";
                refundRequest.setDetailData(refundDetail);
                // 记录支付宝退款操作提交历史
                createRefundHistory(refundRequest, currentUser);
                return MapMessage.successMessage().add("alipayForm", paymentGateway.getRefundRequestForm(refundRequest).generateHtml("refundForm"));
            } else {
                return MapMessage.errorMessage("不支持的支付方式");
            }
        } else {
            // 批量的要判断是否同一个网关
            List<RefundRequest> otherRequest = refundRequestList.stream()
                    .filter(p -> StringUtils.isBlank(p.getPayMethod()) || (!p.getPayMethod().contains("wechatpay") && !p.getPayMethod().contains("alipay")))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(otherRequest)) {
                return MapMessage.errorMessage("不支持的支付方式");
            }
            List<RefundRequest> wechatRequest = refundRequestList.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getPayMethod()) && p.getPayMethod().contains("wechatpay"))
                    .collect(Collectors.toList());
            List<RefundRequest> alipayRequest = refundRequestList.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getPayMethod()) && p.getPayMethod().contains("alipay"))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(wechatRequest) && CollectionUtils.isNotEmpty(alipayRequest)) {
                return MapMessage.errorMessage("请选择相同的支付方式订单进行退款");
            }
            if (CollectionUtils.isNotEmpty(wechatRequest)) {
                // 进行退款微信
                for (RefundRequest refundRequest : wechatRequest) {
                    resultInfos.add(wechatRefund(refundRequest, currentUser, comment));
                }
                return MapMessage.successMessage().add("resultInfos", resultInfos);
            } else {
                Set<String> payMethods = alipayRequest.stream().map(RefundRequest::getPayMethod).collect(Collectors.toSet());
                if (payMethods.size() > 1) {
                    return MapMessage.errorMessage("请选择相同的支付方式进行退款");
                }
                // 进行支付宝操作
                RefundRequest refundRequest = new RefundRequest();
                refundRequest.setPayMethod(payMethods.stream().findFirst().orElse(""));
                refundRequest.setBatchNum(SafeConverter.toString(alipayRequest.size()));
                // 拼装支付宝退款详情
                StringBuilder stringBuilder = new StringBuilder();
                for (RefundRequest request : alipayRequest) {
                    stringBuilder.append(request.getTransactionId()).append("^").append(request.getRefundFee()).append("^")
                            .append("协商退款").append("#");
                    // 记录历史
                    createRefundHistory(request, currentUser);
                }
                String refundDetail = StringUtils.substring(stringBuilder.toString(), 0, stringBuilder.toString().length() - 1);
                refundRequest.setDetailData(refundDetail);
                PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(refundRequest.getPayMethod());
                return MapMessage.successMessage().add("alipayForm", paymentGateway.getRefundRequestForm(refundRequest).generateHtml("refundForm"));
            }
        }
    }


    // 创建支付宝退款提交历史
    private void createRefundHistory(RefundRequest refundRequest, AuthCurrentUser currentUser) {
        OrderRefundHistory history = userOrderLoaderClient.loadOrderRefundHistoryById(refundRequest.getTransactionId());
        // 防止已经处理完了，再提交
        if (history != null && history.getStatus() != RefundHistoryStatus.SUBMIT) {
            return;
        }
        // 已经提交过了，但是没处理  更新时间
        if (history != null) {
            history.setUpdateDatetime(new Date());
        } else {
            history = new OrderRefundHistory();
            history.setUserId(refundRequest.getUserId());
            history.setAgentProcessId(refundRequest.getAgentProcessId());
            history.setAgentUserId(currentUser.getUserId());
            history.setAgentUserName(currentUser.getUserName());
            history.setId(refundRequest.getTransactionId());
            history.setOrderId(refundRequest.getOrderId());
            history.setPayMethod(refundRequest.getPayMethod());
            history.setRefundFee(new BigDecimal(refundRequest.getRefundFee()));
            Date now = new Date();
            history.setCreateDatetime(now);
            history.setUpdateDatetime(now);
            history.setStatus(RefundHistoryStatus.SUBMIT);
        }
        userOrderServiceClient.saveOrUpdateRefundHistory(history);

    }

    private void updatePaymentHistoryStatus(Long userId, PaymentStatus status, String orderId) {
        List<UserOrderPaymentHistory> histories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userId);
        if (CollectionUtils.isEmpty(histories) || status == null || StringUtils.isBlank(orderId)) {
            return;
        }
        // 获取用户对应的流水 通过orderId
        String[] orderIdArray = StringUtils.split(orderId, "_");
        String realOrderId = orderIdArray[0];
        UserOrderPaymentHistory history = histories.stream().filter(h -> Objects.equals(h.getOrderId(), realOrderId))
                .filter(h -> h.getPaymentStatus() == PaymentStatus.Refunding)
                .findFirst().orElse(null);
        if (history == null) {
            return;
        }
        userOrderServiceClient.updatePaymentHistoryStatus(history, status);
    }

    private MapMessage wechatRefund(RefundRequest request, AuthCurrentUser currentUser, String comment) {
        PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(request.getPayMethod());
        if (paymentGateway == null) {
            return MapMessage.errorMessage("不支持的支付方式");
        }
        try {
            WechatAbstractPaymentGateway wechatAbstractPaymentGateway = (WechatAbstractPaymentGateway) paymentGateway;
            MapMessage message = wechatAbstractPaymentGateway.refundOrder(request);
            message.put("orderId", request.getOrderId());
            message.put("transactionId", request.getTransactionId());
            message.put("refundFee", request.getRefundFee());
            PaymentStatus status = PaymentStatus.Refund;
            if (message.isSuccess()) {
                // 自动审批任务 - 通过
                approveOrder(request.getAgentProcessId(), AgentOrderType.REFUND.getType(), currentUser, comment);
                message.put("status", "成功");
            } else {
                // 自动审批任务 - 拒绝
                rejectOrder(request.getAgentProcessId(), currentUser, comment);
                message.put("status", "失败");
                message.put("errorCode", message.getInfo());
                status = PaymentStatus.RefundFail;
            }
            // 更改用户流水退款状态
            updatePaymentHistoryStatus(request.getUserId(), status, request.getOrderId());
            return message;
        } catch (Exception ex) {
            logger.error("Refund order error, agentProcessId is :{}, ex {}", request.getAgentProcessId(), ex.getMessage());
            return MapMessage.errorMessage("退款失败");
        }
    }
}
