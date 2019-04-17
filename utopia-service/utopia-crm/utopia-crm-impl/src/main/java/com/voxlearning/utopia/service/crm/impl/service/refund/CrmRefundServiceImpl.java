/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.crm.impl.service.refund;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.CommonConfig;
import com.voxlearning.utopia.service.crm.api.service.refund.CrmRefundService;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowRefer;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowType;
import com.voxlearning.utopia.service.user.api.mappers.FinanceFlowContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CRM 退款相关调用
 * Created by Yuechen.wang on 2017/1/20.
 */
@Named
@Service(interfaceClass = CrmRefundService.class)
@ExposeService(interfaceClass = CrmRefundService.class)
public class CrmRefundServiceImpl extends SpringContainerSupport implements CrmRefundService {

    private static final String CONFIG_CATEGORY_NAME = ConfigCategory.PRIMARY_PLATFORM_GENERAL.name();
    private static final String ORDER_REFUND_NOTIFY_EMAIL_CC = "ORDER_REFUND_EMAIL_CC";
    private static final String ORDER_REFUND_NOTIFY_EMAIL_KEFU = "ORDER_REFUND_EMAIL_KEFU";
    private static final String WITHDRAW_EMAIL_CC = "chen.ling@17zuoye.com;li.xiao@17zuoye.com";

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private FinanceServiceClient financeServiceClient;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;

    @Override
    public MapMessage refundResult(String orderId, Long userId, String memo, Boolean result) {
        if (StringUtils.isBlank(orderId) || 0L == userId) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            if (result) {
                sendRefundNotify("订单退款成功", StringUtils.formatMessage("用户{}的订单{}已退款成功。", userId, orderId));
                return MapMessage.successMessage();
            } else {
                sendRefundNotify("订单退款失败", StringUtils.formatMessage("用户{}的订单{}退款失败，需要线下处理：发送退款邮件并申请报销。", userId, orderId));
                return MapMessage.successMessage();
            }
        } catch (Exception ex) {
            logger.error("退款操作失败：" + ex.getMessage(), ex);
            return MapMessage.errorMessage("退款回调失败");
        }
    }

    @Override
    public MapMessage withdrawResult(Long userId, Double amount, String memo, Boolean result) {
        try {
            if (result) {
                sendWithdrawNotify("余额提现成功", StringUtils.formatMessage("用户({})提现成功,金额:{},财务已处理完毕", userId, amount));
                return MapMessage.successMessage();
            } else {
                sendWithdrawNotify("余额提现失败", StringUtils.formatMessage("用户({})提现失败,金额:{},财务已拒绝,原因:{}", userId, amount, memo));
                //财务给用户提现失败,把提现金额退回到用户的作业币余额中,并记一条充值流水
                if (createReDepositFinanceFlow(userId, amount, memo)) {
                    return MapMessage.successMessage();
                } else {
                    return MapMessage.errorMessage("回调失败,没有生成财务流水");
                }
            }
        } catch (Exception ex) {
            logger.error("余额提现操作失败：" + ex.getMessage(), ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage loadCrmFinanceFlow(Long userId, String transactionIds) {
        // 测试环境就不打扰了。。。
        if (0 == userId || StringUtils.isBlank(transactionIds)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            List<String> tids = JsonUtils.fromJsonToList(transactionIds, String.class);

            List<UserOrderPaymentHistory> flows = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userId).stream()
                    .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid)
                    .filter(h -> StringUtils.isNotBlank(h.getOuterTradeId()) && tids.contains(h.getOuterTradeId()))
                    .collect(Collectors.toList());
            return MapMessage.successMessage().add("flows", flows);
        } catch (Exception ex) {
            logger.error("Get finance flow failed,tids:{}", transactionIds, ex);
            return MapMessage.errorMessage("查询充值流水失败");
        }
    }

    // 发送退款邮件提醒
    private void sendRefundNotify(String subject, String content) {
        emailServiceClient.createPlainEmail()
                .to(getConfigValue(CONFIG_CATEGORY_NAME, ORDER_REFUND_NOTIFY_EMAIL_KEFU))
                .cc(getConfigValue(CONFIG_CATEGORY_NAME, ORDER_REFUND_NOTIFY_EMAIL_CC))
                .subject(subject)
                .body(content)
                .send();
    }

    // 发送余额提现邮件提醒
    private void sendWithdrawNotify(String subject, String content) {
        // 测试环境就不打扰了。。。
        if (RuntimeMode.current().le(Mode.TEST)) {
            return;
        }
        emailServiceClient.createPlainEmail()
                .to(getConfigValue(CONFIG_CATEGORY_NAME, ORDER_REFUND_NOTIFY_EMAIL_KEFU))
                .cc(WITHDRAW_EMAIL_CC)
                .subject(subject)
                .body(content)
                .send();
    }

    private String getConfigValue(String category, String key) {
        if (StringUtils.isAnyBlank(category, key)) {
            return null;
        }
        CommonConfig config = crmConfigService.$loadCommonConfigs().stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .filter(e -> category.equals(e.getCategoryName()))
                .filter(e -> key.equals(e.getConfigKeyName()))
                .sorted((o1, o2) -> {
                    long u1 = SafeConverter.toLong(o1.getUpdateDatetime());
                    long u2 = SafeConverter.toLong(o2.getUpdateDatetime());
                    return Long.compare(u2, u1);
                }).findFirst().orElse(null);
        if (null == config) return null;
        return config.getConfigKeyValue();
    }

    private boolean createReDepositFinanceFlow(Long userId, BigDecimal depositAmount, String orderId, String memo) {
        FinanceFlowContext context = FinanceFlowContext.instance()
                .userId(userId)
                .type(FinanceFlowType.Deposit)
                .refer(FinanceFlowRefer.CRM)
                .amount(depositAmount)
                .payAmount(depositAmount)
                .state(FinanceFlowState.SUCCESS)
                .orderId(orderId)
                .memo("财务退款失败,原因:" + memo);
        return financeServiceClient.getFinanceService().deposit(context).getUninterruptibly();
    }

    private boolean createReDepositFinanceFlow(Long userId, Double amount, String memo) {
        FinanceFlowContext context = FinanceFlowContext.instance()
                .userId(userId)
                .amount(BigDecimal.valueOf(amount))
                .payAmount(BigDecimal.valueOf(amount))
                .type(FinanceFlowType.Deposit)
                .state(FinanceFlowState.SUCCESS)
                .refer(FinanceFlowRefer.CRM)
                .memo(memo);
        return financeServiceClient.getFinanceService().deposit(context).getUninterruptibly();
    }
}
