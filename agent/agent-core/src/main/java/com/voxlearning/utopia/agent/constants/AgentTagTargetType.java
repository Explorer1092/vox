package com.voxlearning.utopia.agent.constants;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 *
 * @author deliang.che
 * @since  2019/3/22
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentTagTargetType {
    NOTIFY(1, "通知"),
    TEACHER(2, "老师"),
    SCHOOL(3, "学校");

    private final int id;
    private final String desc;

    private final static Map<String, AgentTagTargetType> NAME_MAP = new LinkedHashMap<>();
    static {
        for(AgentTagTargetType tagType : AgentTagTargetType.values()){
            NAME_MAP.put(tagType.name(), tagType);
        }
    }

    public static AgentTagTargetType nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }


}
