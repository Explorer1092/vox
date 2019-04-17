/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 消息产生可能有待审核状态（运营活动）
 * 运营消息入库都是PENDING，
 * 从CRM中审核通过后变为WAITING
 * @author xin
 * @since 14-5-21 下午1:58
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum WechatNoticeState {
    ANONYMOUS(0, "未知类型"),
    WAITTING(1, "待发送"),
    SENDED(4, "已发出"),
    SUCCESS(2, "发送成功"),
    FAILED(3, "发送失败"),
    PENDING(5, "待审核"),
    EXPIRED(6, "已过期");
    private final static Map<Integer, WechatNoticeState> states;
    static {
        states = new LinkedHashMap<>();
        for (WechatNoticeState state : values()) {
            states.put(state.getType(), state);
        }
    }

    @Getter
    private int type;
    @Getter
    private String description;

    WechatNoticeState(int type, String description) {
        this.type = type;
        this.description = description;
    }

    public static WechatNoticeState of(Integer type) {
        if (null == type) {
            return ANONYMOUS;
        }
        return states.get(type);
    }


}
