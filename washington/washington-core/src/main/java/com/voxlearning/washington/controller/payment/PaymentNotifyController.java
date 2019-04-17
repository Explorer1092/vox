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

package com.voxlearning.washington.controller.payment;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentGatewayManager;
import com.voxlearning.utopia.payment.RefundCallbackContext;
import com.voxlearning.utopia.service.business.consumer.BusinessUserOrderServiceClient;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.message.api.MessageCommandService;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import com.voxlearning.utopia.service.userlevel.api.UserLevelService;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 13/5/2016
 */
@Controller
@Slf4j
@RequestMapping(value = "/payment/notify")
public class PaymentNotifyController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private FinanceServiceClient financeServiceClient;
    @Inject
    private PaymentGatewayManager paymentGatewayManager;
    @Inject
    private BusinessUserOrderServiceClient businessUserOrderServiceClient;
    @Inject
    private MizarLoaderClient mizarLoaderClient;

    @ImportService(interfaceClass = MessageCommandService.class)
    private MessageCommandService messageCommandService;
    @ImportService(interfaceClass = UserLevelService.class)
    private UserLevelService userLevelService;

    /**
     * 新订单中心支付回调接口
     */
    @RequestMapping(value = "/order/{paymentGatewayName}-{action}.vpage", method = RequestMethod.POST)
    public void userOrderPaymentNotify(@PathVariable String paymentGatewayName,
                                       @PathVariable String action,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(paymentGatewayName);
        PaymentCallbackContext context = paymentGateway.getPaymentCallbackData(action, request);
        LogCollector.info("backend-general", MiscUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "op", "payment_controller",
                "context", JsonUtils.toJson(context)
        ));
        if (action.equals(PaymentGateway.CallbackAction_Notify)) {
            if (context.getIsValidToProcess()) {
                if (paymentGateway.verifyPaymentCallback(context)) {
                    try {
                        UserOrder userOrder = AtomicLockManager.instance().wrapAtomic(businessUserOrderServiceClient)
                                .keys(context.getVerifiedPaymentData().getTradeNumber(), context.getVerifiedPaymentData().getExternalTradeNumber())
                                .proxy()
                                .processUserOrderPayment(context);
                        if (null != userOrder && userOrder.getPaymentStatus() == PaymentStatus.Paid
                                && userOrder.getOrderStatus() == OrderStatus.Confirmed) {
                            paymentGateway.makeGatewayCallbackResponse(context, response, null);
                        }
                    } catch (CannotAcquireLockException ignore) {

                    } catch (Exception ex) {
                        logger.error("userOrder:" + paymentGatewayName + ":" + action + " proccess failed," + JsonUtils.toJson(context));
                    }
                }
            } else {
                logger.error("userOrder:" + paymentGatewayName + ":" + action + " invalid," + JsonUtils.toJson(context));
            }
        } else {
            logger.error("userOrder:" + paymentGatewayName + ":" + action + " unknown action," + JsonUtils.toJson(context));
        }
    }

    /**
     * 新订单中心退款回调接口  支付宝
     */
    @RequestMapping(value = "/refund/{paymentGatewayName}.vpage", method = RequestMethod.POST)
    public void userOrderRefundNotify(@PathVariable String paymentGatewayName,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(paymentGatewayName);
        RefundCallbackContext context = paymentGateway.getRefundCallbackData(request);
        if (context.getIsValidToProcess()) {
            Map<String, String> params = context.getParams();
            String resultDetailStr = params.get("result_details");
            String[] resultArray = resultDetailStr.split("#");
            List<Map<String, String>> results = new ArrayList<>();
            if (resultArray.length > 0) {
                for (String detailStr : resultArray) {
                    String[] detailArray = detailStr.split("\\^");
                    if (detailArray.length >= 3) {
                        String transactionId = detailArray[0];
                        String result = detailArray[2];
                        Map<String, String> resultMap = new HashMap<>();
                        resultMap.put("transactionId", transactionId);
                        resultMap.put("code", result);
                        results.add(resultMap);
                    }
                }
            }
            try {
                // 发送到order处理
                userOrderServiceClient.dealAliRefundResults(results);
                // 返回响应
                response.getWriter().write("success");
            } catch (IOException e) {
                throw new RuntimeException("", e);
            }
        } else {
            logger.error("userOrder:" + paymentGatewayName + ": refund invalid," + JsonUtils.toJson(context));
        }
    }

    /**
     * 充值支付回调
     */
    @RequestMapping(value = "/recharge/{paymentGatewayName}-{action}.vpage", method = RequestMethod.POST)
    public void rechargePaymentNotify(@PathVariable String paymentGatewayName, @PathVariable String action, HttpServletRequest request, HttpServletResponse response) {
        PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(paymentGatewayName);
        PaymentCallbackContext context = paymentGateway.getPaymentCallbackData(action, request);
        if (action.equals(PaymentGateway.CallbackAction_Notify)) {
            if (context.getIsValidToProcess()) {
                if (paymentGateway.verifyPaymentCallback(context)) {
                    try {
                        BigDecimal amount = context.getVerifiedPaymentData().getPayAmount();
                        boolean rechargeResult = financeServiceClient.getFinanceService()
                                .recharge(context.getTradeNumber(), amount, context.getExternalTradeNumber())
                                .getUninterruptibly();
                        if (!rechargeResult) {
                            logger.error("finance recharge error:" + paymentGatewayName + ":" + action + "," + JsonUtils.toJson(context));
                        }

                        FinanceFlow flow = financeServiceClient.getFinanceService().loadFinanceFlow(context.getTradeNumber()).getUninterruptibly();
                        if (null != flow) {
                            User user = raikouSystem.loadUser(flow.getUserId());
                            if (null != user && user.fetchUserType() == UserType.PARENT) {
                                userLevelService.parentRecharge(user.getId(), flow.getAmount());
                            }
                        }

                        paymentGateway.makeGatewayCallbackResponse(context, response, null);
                    } catch (CannotAcquireLockException ignore) {
                    } catch (Exception ex) {
                        logger.error("finance recharge callback error:" + paymentGatewayName + ":" + action + " process failed," + JsonUtils.toJson(context));
                    }
                } else {
                    logger.error("finance recharge:" + paymentGatewayName + ":" + action + " verify failed," + JsonUtils.toJson(context));
                }
            } else {
                logger.error("finance recharge:" + paymentGatewayName + ":" + action + " invalid," + JsonUtils.toJson(context));
            }
        } else {
            logger.error("finance recharge::" + paymentGatewayName + ":" + action + " unknown action," + JsonUtils.toJson(context));
        }
    }

}
