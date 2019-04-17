package com.voxlearning.wechat.payment.handler;

import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.wechat.payment.PaymentResultContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

/**
 * Created by xinxin on 2/2/2016.
 */
public abstract class NotifyHandler implements InitializingBean, ApplicationContextAware {
    protected ApplicationContext applicationContext;

    public boolean handle(PaymentResultContext context) {
        preHandle(context);

        boolean result = doHandle(context);

        if (result) postHandle(context);

        return result;

    }

    public PaymentCallbackContext parse(PaymentResultContext context) {
        BigDecimal amount = context.getTotalFee();

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("out_trade_no", context.getTransactionId());
        params.put("trade_no", context.getOrderId());
        params.put("total_fee", amount.toString());

        PaymentCallbackContext cxt = new PaymentCallbackContext(PaymentResultContext.WECHAT_PAYMENT_GATEWAY, "notify");
        cxt.setParams(params);
        cxt.setIsValidToProcess(true);

        cxt.setTradeNumber(context.getOrderId());   //一起作业订单号
        cxt.setExternalTradeNumber(context.getTransactionId());     //财付通流水号

        PaymentVerifiedData verifiedData = new PaymentVerifiedData();
        verifiedData.setTradeNumber(cxt.getTradeNumber());
        verifiedData.setExternalTradeNumber(cxt.getExternalTradeNumber());
        verifiedData.setPayAmount(amount);
        cxt.setVerifiedPaymentData(verifiedData);

        return cxt;
    }

    public abstract String getType();

    protected abstract void preHandle(PaymentResultContext context);

    protected abstract boolean doHandle(PaymentResultContext context);

    protected abstract void postHandle(PaymentResultContext context);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
