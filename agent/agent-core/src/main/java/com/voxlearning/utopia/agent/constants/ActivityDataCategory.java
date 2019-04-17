package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityDataCategory {

    COUPON(1, "优惠券"),
    ORDER(2, "订单"),
    ORDER_USER(4, "下单用户"),
    COURSE(3, "上课情况"),
    GROUP(5, "组团数据"),
    GROUP_USER(6, "组团用户"),
    CARD(7, "礼品卡");

    private final Integer id;
    private final String desc;
}
