/*
 *
 *  * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *  *
 *  *  Copyright 2006-2014 Vox Learning Technology, Inc. All Rights Reserved.
 *  *
 *  *  NOTICE: All information contained herein is, and remains the property of
 *  *  Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 *  *  and technical concepts contained herein are proprietary to Vox Learning
 *  *  Technology, Inc. and its suppliers and may be covered by patents, patents
 *  *  in process, and are protected by trade secret or copyright law. Dissemination
 *  *  of this information or reproduction of this material is strictly forbidden
 *  *  unless prior written permission is obtained from Vox Learning Technology, Inc.
 *
 */

package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shuai.Huan on 2014/7/15.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentOrderType {

    WITHDRAW(0, "提现订单"),
    RECHARGE(1, "充值订单"),
    BUY_CARD(2, "充值卡购买"),
    BUY_MATERIAL(3, "材料购买"),
    DEPOSIT(4, "保证金收款"),
    REFUND(5, "CRM退款"),
    CASH_WITHDRAW(6, "CRM余额提现"),
    ;

    @Getter
    private final int type;
    @Getter
    private final String desc;

    public static Map<Integer, AgentOrderType> toMap() {

        Map<Integer, AgentOrderType> map = new HashMap<>();
        for (AgentOrderType agentOrderType : values()) {
            map.put(agentOrderType.type, agentOrderType);
        }
        return map;
    }

    public static AgentOrderType of(Integer type) {
        if (type == null) {
            return null;
        }
        return toMap().get(type);
    }

}
