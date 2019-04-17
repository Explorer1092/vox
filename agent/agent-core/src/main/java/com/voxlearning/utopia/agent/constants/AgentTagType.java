package com.voxlearning.utopia.agent.constants;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 标签类别
 *
 * @author deliang.che
 * @since  2019/3/20
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentTagType {
    NOTIFY(1, "通知"),
    TEACHER(2, "老师"),
    SCHOOL(3, "学校");

    private final int code;
    private final String desc;

    private final static Map<String, AgentTagType> NAME_MAP = new LinkedHashMap<>();
    static {
        for(AgentTagType tagType : AgentTagType.values()){
            NAME_MAP.put(tagType.name(), tagType);
        }
    }

    public static AgentTagType nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }


}
