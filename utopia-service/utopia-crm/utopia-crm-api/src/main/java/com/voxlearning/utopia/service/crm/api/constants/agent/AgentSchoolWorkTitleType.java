package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/18.
 */
@Getter
@RequiredArgsConstructor
public enum AgentSchoolWorkTitleType {

    // 2016年 秋季版的进校记录的枚举
    PROMOTE_REGISTER("1", "促进注册"),
    PROMOTE_ATTESTATION("2", "促进认证"),
    PROMOTE_MONTH_TO_LIVE("3", "促进月活"),
    SEEKING_TO_INTRODUCE("4", "寻求介绍"),

    // 2017年 春季的进校记录的拜访主题的枚举
    PROMOTE_REGISTER_2017("11", "促进注册"),
    PROMOTE_SIGN("12", "促进签约"),
    PROMOTE_MAUC_2017("13", "促进月活"),
    SEEK_INTRODUCE_2017("14", "寻求介绍"),
    AFFIRM_BASIC_INFO("15", "确认基本信息");


    private final static Map<String, AgentSchoolWorkTitleType> crmWorkTitleTypeIdMap = new LinkedHashMap<>();
    private final static Map<String, AgentSchoolWorkTitleType> crmWorkTitleTypeTitleMap = new LinkedHashMap<>();

    static {
        for (AgentSchoolWorkTitleType crmWorkTitleType : AgentSchoolWorkTitleType.values()) {
            crmWorkTitleTypeIdMap.put(crmWorkTitleType.getId(), crmWorkTitleType);
        }

        for (AgentSchoolWorkTitleType crmWorkTitleType : AgentSchoolWorkTitleType.values()) {
            crmWorkTitleTypeTitleMap.put(crmWorkTitleType.getWorkTitle(), crmWorkTitleType);
        }
    }

    private final String id;
    private final String workTitle;

    public static AgentSchoolWorkTitleType of(String id) {
        if (id == null) {
            return null;
        }
        return crmWorkTitleTypeIdMap.get(id);
    }

    public static AgentSchoolWorkTitleType titleOf(String title) {
        if (title == null) {
            return null;
        }
        return crmWorkTitleTypeTitleMap.get(title);
    }
}
