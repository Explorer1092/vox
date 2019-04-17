package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum ActivityDataIndicator {
    COUPON_DAY(ActivityDataCategory.COUPON, "当日优惠券"),
    COUPON_SUM(ActivityDataCategory.COUPON, "累计优惠券"),

    ORDER_DAY(ActivityDataCategory.ORDER, "当日订单"),
    ORDER_SUM(ActivityDataCategory.ORDER, "累计订单"),
    ORDER_USER_DAY(ActivityDataCategory.ORDER_USER, "当日购课用户"),
    ORDER_USER_SUM(ActivityDataCategory.ORDER_USER, "累计购课用户"),

    COURSE_SUM(ActivityDataCategory.COURSE, "累计上课用户"),
    COURSE_MEET_SUM(ActivityDataCategory.COURSE, "满足指定条件累计上课用户"),    // 上课>=3天等指定条件

    GROUP_DAY(ActivityDataCategory.GROUP, "当日组团"),    //
    GROUP_SUM(ActivityDataCategory.GROUP, "累计组团"),    //

    GROUP_COMPLETE_DAY(ActivityDataCategory.GROUP, "当日成功组团"),
    GROUP_COMPLETE_SUM(ActivityDataCategory.GROUP, "累计成功组团"),

    GROUP_USER_DAY(ActivityDataCategory.GROUP_USER, "当日参与组团用户数"),
    GROUP_USER_SUM(ActivityDataCategory.GROUP_USER, "累计参与组团用户数"),

    GROUP_COMPLETE_USER_DAY(ActivityDataCategory.GROUP_USER, "当日成功组团用户数"),
    GROUP_COMPLETE_USER_SUM(ActivityDataCategory.GROUP_USER, "累计成功组团用户数"),

    CARD_DAY(ActivityDataCategory.CARD, "当日领取礼品卡"),
    CARD_SUM(ActivityDataCategory.CARD, "累计领取礼品卡"),

    CARD_USED_DAY(ActivityDataCategory.CARD, "当日使用礼品卡"),
    CARD_USED_SUM(ActivityDataCategory.CARD, "累计使用礼品卡"),
    ;



    private final ActivityDataCategory category;
    private final String desc;

    public static Map<String, ActivityDataIndicator> indicatorMap = new HashMap<>();
    static {
        for(ActivityDataIndicator indicator : ActivityDataIndicator.values()){
            indicatorMap.put(indicator.name(), indicator);
        }
    }

    public static ActivityDataIndicator nameOf(String name){
        return indicatorMap.get(name);
    }
}
