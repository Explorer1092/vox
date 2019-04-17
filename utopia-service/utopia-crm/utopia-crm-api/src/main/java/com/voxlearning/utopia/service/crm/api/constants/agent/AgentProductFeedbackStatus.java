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
public enum AgentProductFeedbackStatus {

    SO_PENDING(1, "客服处理中"), // sales operator
    SO_REJECTED(2, "销运未采纳"),
    PM_PENDING(3, "PM处理中"),
    PM_REJECTED(4, "PM未采纳"),
    PM_APPROVED(5, "PM已采纳"),
    PM_EVALUATING(6, "PM评估中"),
    SO_CONFIRMING(7, "销运确认中")
    ;
    @Getter
    private final int id;
    @Getter
    private final String desc;

    private final static Map<Integer, AgentProductFeedbackStatus> statusMap = new HashMap<>();
    static {
        for (AgentProductFeedbackStatus status : AgentProductFeedbackStatus.values()) {
            statusMap.put(status.getId(), status);
        }
    }

    public static AgentProductFeedbackStatus of(Integer type) {
        return statusMap.get(type);
    }

}
