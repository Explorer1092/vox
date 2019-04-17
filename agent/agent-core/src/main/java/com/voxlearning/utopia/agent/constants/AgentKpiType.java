package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  业绩指标类型
 *
 * @author song.wang
 * @date 2017/3/27
 */
public enum AgentKpiType {

    JUNIOR_ENG_ADD(10, "小英新增", true),
    JUNIOR_MATH_ADD(11, "小数新增", true),
    JUNIOR_CHN_ADD(12, "小语新增", true),
    JUNIOR_SGL_SUBJ_ADD(13, "小单新增", true),
    MIDDLE_ENG_ADD(20, "中英新增", true),
    MIDDLE_ENG_BF(21, "中英回流", true),
    MIDDLE_MATH_ADD(22, "中数新增", true),
    MIDDLE_SGL_SUBJ_ADD(23, "中学单科新增", true),
    STU_PARENT_ACTIVE(24, "学生家长双活", true)

    ;

    @Getter
    private final int type;
    @Getter
    private final String desc;
    @Getter
    private final boolean valid;  // 当前指标是否有效， 如果不在考核该指标了， 可设为false

    AgentKpiType(int type, String desc, boolean valid){
        this.type = type;
        this.desc = desc;
        this.valid = valid;
    }

    private static final Map<String, AgentKpiType> descMap = new HashMap<>();
    private static final Map<Integer, AgentKpiType> typeMap = new HashMap<>();
    static {
        for (AgentKpiType kpiType : AgentKpiType.values()){
            descMap.put(kpiType.getDesc(), kpiType);
            typeMap.put(kpiType.getType(), kpiType);
        }
    }

    public static List<AgentKpiType> fetchValidTypeList(){
        List<AgentKpiType> list = Arrays.asList(AgentKpiType.values());
        return list.stream().filter(AgentKpiType::isValid).collect(Collectors.toList());
    }

    public static AgentKpiType descOf(String desc){
        return descMap.get(desc);
    }

    public static AgentKpiType typeOf(Integer type){
        return typeMap.get(type);
    }

}
