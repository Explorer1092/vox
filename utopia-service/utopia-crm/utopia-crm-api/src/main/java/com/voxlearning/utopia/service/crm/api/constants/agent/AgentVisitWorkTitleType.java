package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by yaguang.wang on 2016/7/18.
 */
@Getter
@RequiredArgsConstructor
public enum AgentVisitWorkTitleType {
    SKILLS_COACHING("1", "专业技能辅导"),
    SCHOOLS_FOLLOW("2", "重点学校跟进"),
    MARKET_CONDITIONS("3", "市场情况了解"),

    SKILL_COUNSELING("4","技能辅导"),
    ASSIST_MAINTENANCE("5","协助维护"),
    UNDERSTAND_MARKET("6","了解市场");

    private final static Map<String, AgentVisitWorkTitleType> crmWorkTitleTypeIdMap = new LinkedHashMap<>();
    private final static Map<String, AgentVisitWorkTitleType> crmWorkTitleTypeNameMap = new LinkedHashMap<>();

    private final String id;
    private final String workTitle;

    static {
        for (AgentVisitWorkTitleType crmWorkTitleType : AgentVisitWorkTitleType.values()) {
            crmWorkTitleTypeIdMap.put(crmWorkTitleType.getId(), crmWorkTitleType);
        }

        for (AgentVisitWorkTitleType crmWorkTitleType : AgentVisitWorkTitleType.values()) {
            crmWorkTitleTypeNameMap.put(crmWorkTitleType.name(), crmWorkTitleType);
        }
    }

    public static AgentVisitWorkTitleType of(String id) {
        if (id == null) {
            return null;
        }
        return crmWorkTitleTypeIdMap.get(id);
    }

    public static AgentVisitWorkTitleType nameOf(String name) {
        if (name == null) {
            return null;
        }
        return crmWorkTitleTypeNameMap.get(name);
    }
}
