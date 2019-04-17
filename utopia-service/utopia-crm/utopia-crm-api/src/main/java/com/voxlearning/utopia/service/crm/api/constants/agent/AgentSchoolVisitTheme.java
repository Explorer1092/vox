package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 进校拜访主题
 */
@Getter
@RequiredArgsConstructor
public enum AgentSchoolVisitTheme {
    PROMOTE_REGISTER("促进注册"),
    PROMOTE_SIGN("促进签约"),
    PROMOTE_MAU("促进认证"),
    TRANSFER_INTRODUCTION("转介绍");

    private final static Map<String, AgentSchoolVisitTheme> NAME_MAP = new LinkedHashMap<>();

    private final String desc;

    static {
        for (AgentSchoolVisitTheme crmWorkTitleType : AgentSchoolVisitTheme.values()) {
            NAME_MAP.put(crmWorkTitleType.name(), crmWorkTitleType);
        }

    }

    public static AgentSchoolVisitTheme nameOf(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return NAME_MAP.get(name);
    }
}
