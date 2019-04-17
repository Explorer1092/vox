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

package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yanling.lan on 2016/1/21.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum OrderResult {
    AFENTI_ORDER_RESULT(0, "恭喜您已成功购买{}，请在{}登录使用，请勿重复购买。", "", "ORDER_VIEW"),
    INTEGRAL_ORDER_RESULT(1, "恭喜您已成功购买班级学豆，老师会在智慧课堂奖励学生", "", "NONE_VIEW"),
    @Deprecated
    GLOBAL_MATH(2, "恭喜您已成功购买${productName}，请在电脑登录一起作业使用", "", "WEB_VIEW"),
    @Deprecated
    TRUSTEECLS(3, "恭喜您已成功购买${productName}，请查看订单联系托管机构使用", "/parent/trustee/orderlist.vpage", "WEB_VIEW"),
    YXP_TRUSTEE_ORDER_RESULT(4, "${content}", "", "ORDER_VIEW"),
    SEATTLE_ORDER_RESULT(5, "", "", "WEB_VIEW"),
    FAMILY_ORDER_RESULT(6, "${content}", "/mizar/familyactivity/paiddetail.vpage", "WEB_VIEW"),
    PICLISTEN_ORDER_RESULT(7, "恭喜您已成功购买{}，请在{}登录使用，请勿重复购买。", "", "NONE_VIEW"),
    PICLISTENBOOK_ORDER_RESULT(8, "恭喜您已成功购买{}，请在{}登录使用，请勿重复购买。", "/view/mobile/activity/parent/point_read_introduce/index.vpage", "ORDER_VIEW"),
    YIQIXUE_ORDER_RESULT(9, "恭喜您已成功购买{}，请在{}登录使用，请勿重复购买。", "/m/order/confirm.vpage#/success/", "WEB_VIEW")
    ;

    public final int id;
    public final String content;
    public final String url;
    public final String type;

    public static final Map<Integer, OrderResult> value_map;

    static {
        value_map = new HashMap<>();
        for (OrderResult type : OrderResult.values()) {
            value_map.put(type.getId(), type);
        }
    }

    public static OrderResult of(int id) {
        try {
            return value_map.get(id);
        } catch (Exception e) {
            return null;
        }
    }
}
