package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 服务范围
 * @author deliang.che
 * @since 2018/8/24
 */
@Getter
public enum AgentServiceRange {
    EXAM_SERVICE_PRODUCT("考试服务产品"),
    ONLINE_HOMEWORK_PRODUCT("在线作业产品");

    private final String desc;

    private final static Map<String, AgentServiceRange> NAME_MAP = new LinkedHashMap<>();
    static {
        for(AgentServiceRange serviceType : AgentServiceRange.values()){
            NAME_MAP.put(serviceType.name(), serviceType);
        }
    }

    AgentServiceRange(String desc) {
        this.desc = desc;
    }

    public static AgentServiceRange nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }

    public static List<AgentServiceRange> toList(String serviceRangesStr){
        List<AgentServiceRange> serviceRangeList = new ArrayList<>();
        if(StringUtils.isNotBlank(serviceRangesStr)){
            Set<String> serviceRanges = Arrays.stream(StringUtils.split(serviceRangesStr, ",")).collect(Collectors.toSet());
            for(String serviceTypeStr : serviceRanges){
                AgentServiceRange range = nameOf(StringUtils.trim(serviceTypeStr));
                if(range != null){
                    serviceRangeList.add(range);
                }
            }
        }
        return serviceRangeList;
    }

}
