package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 竞品付费模式枚举类
 * @author deliang.che
 * @date 2018/3/8
 */
@Getter
public enum PaymentModeType {


    ONE_TIME_PURCHASE("一次性采购"),
    YEAR_PAY("年付"),
    USER_PAY("向用户收费"),
    OTHER("其他");


    public final String value;

    private static Map<String, PaymentModeType> descMap = new HashMap<>();
    static {
        for (PaymentModeType item : PaymentModeType.values()) {
            descMap.put(item.getValue(), item);
        }
    }

    PaymentModeType(String value) {
        this.value = value;
    }

    public static PaymentModeType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static PaymentModeType descOf(String name){
        return descMap.get(name);
    }

}
