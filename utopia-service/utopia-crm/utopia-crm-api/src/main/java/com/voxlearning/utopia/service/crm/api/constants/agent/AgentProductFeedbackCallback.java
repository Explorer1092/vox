package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 产品反馈的状态
 *
 * @author song.wang
 * @date 2017/2/21
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentProductFeedbackCallback {

    NEED_CALLBACK(true, "是"),
    NONEED_CALLBACK(false, "否")
    ;
    @Getter
    private final Boolean id;
    @Getter
    private final String desc;

    private final static Map<Boolean, AgentProductFeedbackCallback> statusMap = new HashMap<>();
    static {
        for (AgentProductFeedbackCallback status : AgentProductFeedbackCallback.values()) {
            statusMap.put(status.getId(), status);
        }
    }

    public static AgentProductFeedbackCallback of(Boolean callback) {
        return statusMap.get(callback);
    }

}
