/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.afenti;

import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.washington.support.AbstractController;

import java.math.BigDecimal;


public class AfentiBaseController extends AbstractController {

    protected PaymentGateway fillPaymentRequestCreatePaymentGateway(PaymentRequest paymentRequest, String payMethod, UserOrder order) {
        Long userId = order.getUserId();
        // 这里要判断是否要对价格做处理
        BigDecimal totalPrice = userOrderServiceClient.getOrderCouponDiscountPrice(order);
        if (userId != null && PaymentGateway.getUsersForPaymentTest(userId)) {
            totalPrice = new BigDecimal(0.01);
        }
        // 无法计算有效价格
        if (totalPrice == null) {
            return null;
        }
        paymentRequest.setPayAmount(totalPrice);
        paymentRequest.setProductName(order.getProductName());
        paymentRequest.setPayMethod(payMethod);
        paymentRequest.setTradeNumber(order.genUserOrderId());
        paymentRequest.setCallbackBaseUrl(ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order");
        return paymentGatewayManager.getPaymentGateway(payMethod);
    }
}
