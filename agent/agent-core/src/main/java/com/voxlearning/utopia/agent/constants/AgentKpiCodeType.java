package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by Alex on 14-7-28.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentKpiCodeType {

    ADD_STUDENT_AUTH_NUM(1, "新增认证用户"),
    ONLINE_PAYMENT_SHARE(2, "线上付费数据");

    @Getter
    private final int type;
    @Getter
    private final String desc;

    public static Map<Integer, AgentKpiCodeType> toMap() {

        Map<Integer, AgentKpiCodeType> map = new HashMap<>();
        for (AgentKpiCodeType kpiCodeType : values()) {
            map.put(kpiCodeType.type, kpiCodeType);
        }
        return map;
    }

    public static AgentKpiCodeType parse(Integer kpiCode) {
        return toMap().get(kpiCode);
    }

    public static AgentKpiCodeType of(String value) {
        try {
            return AgentKpiCodeType.valueOf(value);
        } catch (Exception ignored) {
            return ADD_STUDENT_AUTH_NUM;
        }
    }
}