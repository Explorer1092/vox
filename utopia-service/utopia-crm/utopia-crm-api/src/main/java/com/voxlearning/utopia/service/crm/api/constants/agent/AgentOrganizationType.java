package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 机构类别
 * @author deliang.che
 * @since 2019/1/11
 */
@Getter
public enum AgentOrganizationType {
    STAFF_ROOM("教研室"),
    EDUCATION_BUREAU("教育局（厅）"),
    EDUCATION_COMMISSION("教委"),
    ELECTRICAL_EDUCATION_HALL("电教馆"),
    SOCIETY("学会"),
    PUBLISHING_COMPANY("出版社"),
    MEDIUM("媒体"),
    FAMOUS_TEACHER_STUDIO("名师工作室"),
    FAMOUS_SCHOOLMASTER_STUDIO("名校长工作室"),
    OTHER("其他");

    private final String desc;

    private final static Map<String, AgentOrganizationType> NAME_MAP = new LinkedHashMap<>();
    static {
        for(AgentOrganizationType serviceType : AgentOrganizationType.values()){
            NAME_MAP.put(serviceType.name(), serviceType);
        }
    }

    AgentOrganizationType(String desc) {
        this.desc = desc;
    }

    public static AgentOrganizationType nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }


}
