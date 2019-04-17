package com.voxlearning.utopia.service.reward.constant;

import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import lombok.Getter;

import java.util.Objects;

/**
 * @description: 一级分类类型
 * @author: kaibo.he
 * @create: 2018-10-26 18:54
 **/
public enum OneLevelCategoryType {
    JPZX_UNKNOWN(-1, -1, -1, "未知类型"),
    JPZX_SHIWU(1, IntegralType.REWARD_SWGOOD_EXCHANGE_INTEGRAL.getType(), IntegralType.REWARD_SWGOOD_INCOME_INTEGRAL.getType(), "实物"),
    JPZX_FLOW_PACKET(2, IntegralType.REWARD_FLOWPACKET_EXCHANGE_INTEGRAL.getType(), IntegralType.REWARD_FLOWPACKET_INCOME_INTEGRAL.getType(), "流量包"),
    JPZX_HEADWEAR(3, IntegralType.REWARD_XNGOOD_EXCHANGE_INTEGRAL.getType(), IntegralType.REWARD_XNGOOD_INCOME_INTEGRAL.getType(), "头饰"),
    JPZX_MINI_COURSE(4, IntegralType.REWARD_MINICOURSE_EXCHANGE_INTEGRAL.getType(), IntegralType.REWARD_MINICOURSE_INCOME_INTEGRAL.getType(), "微课"),
    JPZX_COUPON(5, IntegralType.REWARD_COUPON_EXCHANGE_INTEGRAL.getType(), IntegralType.REWARD_COUPON_INCOME_INTEGRAL.getType(), "优惠券"),
    JPZX_TEACHING_RESOURCES(6, IntegralType.REWARD_TEACHINGRESOURCES_EXCHANGE_INTEGRAL.getType(), IntegralType.REWARD_TEACHINGRESOURCES_INCOME_INTEGRAL.getType(), "教学资源"),
    JPZX_TOBY(7, IntegralType.REWARD_TOBY_INTEGRAL.getType(), IntegralType.REWARD_TOBY_INCOME_INTEGRAL.getType(), "托比装扮"),
    ;

    private Integer type;
    private String name;
    @Getter
    private Integer integralExchangeType;
    @Getter
    private Integer integralIncomeType;
    OneLevelCategoryType(Integer type, Integer integralExchangeType, Integer integralIncomeType, String name) {
        this.name = name;
        this.type = type;
        this.integralExchangeType = integralExchangeType;
        this.integralIncomeType = integralIncomeType;
    }

    public static OneLevelCategoryType of(Integer type) {
        for (OneLevelCategoryType oneLevelCategoryType : values()) {
            if (Objects.equals(oneLevelCategoryType.intType(), type)) {
                return oneLevelCategoryType;
            }
        }
        return JPZX_UNKNOWN;
    }

    public Integer intType() {
        return type;
    }

}
