package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 业务类型
 * @author deliang.che
 * @since 2018/6/12
 */
@Getter
public enum AgentServiceType {
    PRE_SCHOOL("学前"),
    JUNIOR_SCHOOL("小学"),
    MIDDLE_SCHOOL("初中"),
    SENIOR_SCHOOL("高中");

    private final String typeName;

    private final static Map<String, AgentServiceType> NAME_MAP = new LinkedHashMap<>();
    static {
        for(AgentServiceType serviceType : AgentServiceType.values()){
            NAME_MAP.put(serviceType.name(), serviceType);
        }
    }

    AgentServiceType(String typeName) {
        this.typeName = typeName;
    }

    public static AgentServiceType nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }

    public static List<AgentServiceType> toTypeList(String serviceTypesStr){
        List<AgentServiceType> typeList = new ArrayList<>();
        if(StringUtils.isNotBlank(serviceTypesStr)){
            Set<String> serviceTypes = Arrays.stream(StringUtils.split(serviceTypesStr, ",")).collect(Collectors.toSet());
            for(String serviceTypeStr : serviceTypes){
                AgentServiceType type = nameOf(StringUtils.trim(serviceTypeStr));
                if(type != null){
                    typeList.add(type);
                }
            }
        }
        return typeList;
    }

    public SchoolLevel toSchoolLevel(){
        if(this == PRE_SCHOOL){
            return SchoolLevel.INFANT;
        }else if(this == JUNIOR_SCHOOL){
            return SchoolLevel.JUNIOR;
        }else if(this == MIDDLE_SCHOOL){
            return SchoolLevel.MIDDLE;
        }else if(this == SENIOR_SCHOOL){
            return SchoolLevel.HIGH;
        }
        return null;
    }

}
