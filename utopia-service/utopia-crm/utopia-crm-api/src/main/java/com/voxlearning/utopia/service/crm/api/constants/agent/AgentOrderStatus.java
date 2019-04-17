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
public enum AgentOrderStatus {

    DRAFT(0, "订单草稿"),
    INIT(1, "订单创建"),
    PENDING_FINANCIAL(2, "等待财务确认收款"),
    PENDING_REGION_MANAGER(3, "等待发货"),
    APPROVED(4, "订单通过"),
    REJECTED(5, "订单拒绝"),
    CONFIRMED(6, "订单确认"),
    CANCELED(7, "订单取消"),
    FINISHED(8, "订单结束"),
    DELIVERED(9, "已发货"),
    UNCHECKED(10, "待审核"),
    DELETED(99, "订单删除");


    @Getter
    private final int status;
    @Getter
    private final String desc;

    public static Map<Integer, AgentOrderStatus> toMap() {

        Map<Integer, AgentOrderStatus> map = new HashMap<>();
        for (AgentOrderStatus agentOrderStatus : values()) {
            map.put(agentOrderStatus.status, agentOrderStatus);
        }
        return map;
    }

    public static AgentOrderStatus of(Integer status) {
        if (status == null) {
            return null;
        }
        return toMap().get(status);
    }

}