package com.voxlearning.wechat.payment;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.wechat.support.utils.WechatSignUtils;
import com.voxlearning.utopia.core.helper.XmlConvertUtils;
import lombok.Getter;
import org.dom4j.DocumentException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xinxin on 2/2/2016.
 */
public class PaymentResultContext {
    public static final String WECHAT_OUT_TRADE_NO = "out_trade_no";
    public static final String WECHAT_TRANSACTION_ID = "transaction_id";
    public static final String WECHAT_PAYMENT_GATEWAY = "wechatpay";

    public static final String FIELD_TOTAL_FEE = "total_fee";
    public static final String FIELD_OPEN_ID = "openid";

    @Getter
    Map<String, Object> paramMap;

    public PaymentResultContext(String response) throws DocumentException {
        paramMap = new HashMap<>();
        paramMap.putAll(XmlConvertUtils.toMap(response));
    }

    public boolean validateSign() {
        return !MapUtils.isEmpty(paramMap) && paramMap.get("sign").equals(WechatSignUtils.md5Sign(paramMap).toUpperCase());
    }

    public boolean isPayReturnOk() {
        return !MapUtils.isEmpty(paramMap) && "SUCCESS".equals(paramMap.get("return_code"));
    }

    public boolean isPayResultOk() {
        return !MapUtils.isEmpty(paramMap) && "SUCCESS".equals(paramMap.get("result_code"));
    }

    public String getOrderId() {
        Object orderId = paramMap.get(WECHAT_OUT_TRADE_NO);
        if (null == orderId) throw new IllegalStateException("Invalid orderId from payment notify context.");
        return SafeConverter.toString(orderId);
    }

    public String getTransactionId() {
        Object orderId = paramMap.get(WECHAT_TRANSACTION_ID);
        if (null == orderId) throw new IllegalStateException("Invalid transactionId from payment notify context.");

        return orderId.toString();
    }

    public BigDecimal getTotalFee() {
        return new BigDecimal(paramMap.get(FIELD_TOTAL_FEE).toString()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);//微信支付金额以分计
    }

    public String getOpenId() {
        Object openId = paramMap.get(FIELD_OPEN_ID);
        if (null == openId) return ""; //OpenId是可选字段

        return openId.toString();
    }
}
