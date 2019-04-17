package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付方式枚举
 * Created by yaguang.wang on 2017/1/11.
 */
@Getter
@AllArgsConstructor
public enum AgentOrderPaymentMode {
    MATERIAL_COST(1, "物料费用"),
    CITY_COST(2, "城市支持费用"),
    SELF_COST(3, "自付");
    private Integer payId;
    private String payDes;

    private static final Map<Integer, AgentOrderPaymentMode> paymentMode;

    static {
        paymentMode = new HashMap<>();
        Arrays.stream(AgentOrderPaymentMode.values()).forEach(p -> {
            paymentMode.put(p.getPayId(), p);
        });
    }

    public static AgentOrderPaymentMode safePayIdToMode(Integer payId, AgentOrderPaymentMode defaultValue) {
        if (!paymentMode.containsKey(payId)) {
            return defaultValue;
        }
        AgentOrderPaymentMode result = paymentMode.get(payId);
        return result == null ? defaultValue : result;
    }
}
