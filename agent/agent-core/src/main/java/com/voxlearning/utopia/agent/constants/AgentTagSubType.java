package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签子类别
 *
 * @author deliang.che
 * @since  2019/3/23
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentTagSubType {
    POSITION(1, "职务类"),
    GRADE(2, "等级类"),
    SETTLEMENT(3, "结算类"),
    OPERATION(4, "运营类"),
    OTHER(5, "其他类");

    private final int code;
    private final String desc;

    private final static Map<Integer, AgentTagSubType> CODE_MAP = new LinkedHashMap<>();
    static {
        for(AgentTagSubType tagSubType : AgentTagSubType.values()){
            CODE_MAP.put(tagSubType.getCode(), tagSubType);
        }
    }

    public static AgentTagSubType codeOf(Integer code) {
        return CODE_MAP.get(code);
    }

    public static List<AgentTagSubType> fetchTagSubTypes(){
        List<AgentTagSubType> list = new ArrayList<>();
        list.add(AgentTagSubType.POSITION);
        list.add(AgentTagSubType.GRADE);
        list.add(AgentTagSubType.SETTLEMENT);
        list.add(AgentTagSubType.OPERATION);
        list.add(AgentTagSubType.OTHER);
        return list;
    }


}
