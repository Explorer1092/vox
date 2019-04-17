package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * AgentProductFeedbackSubject
 *
 * @author song.wang
 * @date 2017/2/21
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentProductFeedbackSubject {

    JUNIOR_ENGLISH(1, "小学英语"),
    JUNIOR_MATH(2, "小学数学"),
    JUNIOR_CHINESE(3, "小学语文"),
    MIDDLE_ENGLISH(4, "初中英语"),
    MIDDLE_MATH(5, "初中数学"),
    HIGH_MATH(6, "高中数学"),
    MIDDLE_HIGH_OTHERS(7, "初高中其他")

    ;
    @Getter
    private final int id;
    @Getter
    private final String desc;

    private final static Map<Integer, AgentProductFeedbackSubject> subjectMap = new HashMap<>();
    static {
        for (AgentProductFeedbackSubject subject : AgentProductFeedbackSubject.values()) {
            subjectMap.put(subject.getId(), subject);
        }
    }

    public static AgentProductFeedbackSubject of(Integer type) {
        return subjectMap.get(type);
    }
}
