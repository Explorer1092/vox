package com.voxlearning.wechat.payment;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.wechat.payment.handler.NotifyHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by xinxin on 2/2/2016.
 */
@Slf4j
public class PaymentNotifyProcessor {
    private PaymentResultContext context;
    private NotifyHandler handler;

    public PaymentNotifyProcessor(PaymentResultContext context, NotifyHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    public boolean isValidResult() {
        return context.validateSign();
    }

    public boolean isPaymentSuccess() {
        return context.isPayReturnOk() && context.isPayResultOk();
    }

    public String process() {

        boolean result = handler.handle(context);

        if (!result) {
            log.warn("Wechat payment notify process failed:{}", JsonUtils.toJson(context));
        }

        return processResult(result);
    }

    public String processResult(boolean result) {
        return "<xml>\n" +
                "  <return_code><![CDATA[" + (result ? "SUCCESS" : "FAIL") + "]]></return_code>\n" +
                "  <return_msg><![CDATA[" + (result ? "OK" : "支付结果通知失败") + "]]></return_msg>\n" +
                "</xml>";
    }
}
