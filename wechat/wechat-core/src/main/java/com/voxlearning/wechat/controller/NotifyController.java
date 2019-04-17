/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.controller;

import com.voxlearning.wechat.payment.NotifyHandlerFactory;
import com.voxlearning.wechat.payment.PaymentNotifyProcessor;
import com.voxlearning.wechat.payment.PaymentResultContext;
import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Xin Xin
 * @since 11/2/15
 * <p>
 * 这个Controller是没有身份验证的,注意安全
 */
@Controller
@RequestMapping(value = "/")
public class NotifyController extends AbstractController {
    @RequestMapping(value = "/notify-{type}.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Object notifys(@PathVariable String type, HttpServletRequest request) throws IOException {
        String responseStr = IOUtils.toString(request.getInputStream(), "UTF-8");
        logger.debug("Order payment notify:{},type:{}", responseStr, type);

        try {
            return handleOrder(responseStr, type);
        } catch (Exception ex) {
            logger.error("Order notify process error,{}:{}", type, responseStr, ex);
        }

        return payCallbackResult(false, "支付通知失败");
    }

    private String handleOrder(String result, String type) throws DocumentException {
        PaymentResultContext context = new PaymentResultContext(result);

        PaymentNotifyProcessor processor = new PaymentNotifyProcessor(context, NotifyHandlerFactory.getHandler(type));
        if (!processor.isValidResult()) { //较验签名
            return processor.processResult(false);
        }

        if (processor.isPaymentSuccess()) { //是否支付成功
            return processor.process();
        } else {
            logger.info("Payment not success,{}:{}", type, result);
        }

        return processor.processResult(false);
    }

    @Deprecated
    public String payCallbackResult(boolean success, String msg) {
        return "<xml>\n" +
                "  <return_code><![CDATA[" + (success ? "SUCCESS" : "FAIL") + "]]></return_code>\n" +
                "  <return_msg><![CDATA[" + (success ? "OK" : msg) + "]]></return_msg>\n" +
                "</xml>";
    }
}
