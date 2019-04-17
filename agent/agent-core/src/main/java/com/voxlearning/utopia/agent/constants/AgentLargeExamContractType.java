package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by dell on 2018/3/13.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentLargeExamContractType {
    PAY_EXAM(1, "付费"),
    LARGE_EXAM(2, "大考");

    @Getter
    private final int type;
    @Getter
    private final String desc;


    public static AgentLargeExamContractType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
