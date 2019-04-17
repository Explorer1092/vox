package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AgentSchoolPermeabilityType
 *
 * @author song.wang
 * @date 2017/8/7
 */
@Getter
@AllArgsConstructor
public enum AgentSchoolPermeabilityType {
    LOW("低渗"),
    MIDDLE("中渗"),
    HIGH("高渗"),
    SUPER_HIGH("超高渗");

    private final String desc;
    private final static Map<String, AgentSchoolPermeabilityType> typeMap = new HashMap<>();
    private final static Map<String, AgentSchoolPermeabilityType> nameMap = new HashMap<>();

    static {
        for (AgentSchoolPermeabilityType type : AgentSchoolPermeabilityType.values()) {
            typeMap.put(type.desc, type);
            nameMap.put(type.name(), type);
        }
    }

    public static AgentSchoolPermeabilityType of(String level) {
        if (StringUtils.isNotBlank(level)){
            return typeMap.get(level);
        }
        return null;
    }

    public static AgentSchoolPermeabilityType nameOf(String name) {
        if (StringUtils.isNotBlank(name)){
            return nameMap.get(name);
        }
        return null;
    }

    public static List<AgentSchoolPermeabilityType> viewPermeability() {
        return Arrays.asList(AgentSchoolPermeabilityType.values());
    }
}
