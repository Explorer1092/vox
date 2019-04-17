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

package com.voxlearning.washington.controller.afenti;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.payment.PaymentGateway;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/apps/afenti/payment")

public class AfentiPaymentController extends AfentiBaseController {

    // // FIXME: 2017/3/6  以前PC支付的回调都会走这里， 现在去掉了。统一走PaymentNotifyController
    @RequestMapping(value = "{paymentGatewayName}-{callbackAction}.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public void callback(Model model, @PathVariable final String paymentGatewayName, @PathVariable final String callbackAction, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(paymentGatewayName);
        PaymentCallbackContext context = paymentGateway.getPaymentCallbackData(callbackAction, httpServletRequest);

        String json = JsonUtils.toJson(context);
        if (StringUtils.isNotBlank(json) && !json.contains("WAIT_BUYER_PAY") && !json.contains("TRADE_FINISHED")
                && !json.contains("TRADE_CLOSED") && !json.contains("TRADE_SUCCESS")) {
            logger.error("error callBack request, context is " + json);
        }

//
//        if (callbackAction.equals(PaymentGateway.CallbackAction_Return) || callbackAction.equals(PaymentGateway.CallbackAction_Notify)) {
//            if (context.getIsValidToProcess()) {
//                if (paymentGateway.verifyPaymentCallback(context)) {
//                    model.addAttribute("verifiedPaymentData", context.getVerifiedPaymentData());
//                    businessUserOrderServiceClient.processUserOrderPayment(context);
//                }
//            }
//        } else {
//            logger.error(callbackAction + "-" + paymentGatewayName + ": " + context.getParams().toString());
//        }
//
//
//        String redirectUrl = "redirect:/apps/afenti/order/finished.vpage";
//        redirectUrl += "?orderId=" + context.getTradeNumber();
//
//        //send response
//        if (paymentGateway.makeGatewayCallbackResponse(context, httpServletResponse, redirectUrl)) {
//            return;
//        }
//
//        try {
//            httpServletResponse.sendRedirect(redirectUrl);
//        } catch (Exception e) {
//            // ignore this error
//        }

    }

}
