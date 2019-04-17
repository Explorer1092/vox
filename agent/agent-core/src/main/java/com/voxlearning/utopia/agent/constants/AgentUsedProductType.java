package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 市场活动的入口类型
 * Created by yaguang.wang on 2016/8/2.
 */
@Getter
@RequiredArgsConstructor
public enum AgentUsedProductType {

    TEACHER_PC_PORT(1, "老师PC端"),
    TEACHER_APP_PORT(2, "老师APP端"),
    TEACHER_WECHAT_PORT(3, "老师微信端"),
    STUDENT_PC_PORT(4, "学生PC端"),
    STUDENT_APP_PORT(5, "学生APP端"),
    PARENT_WECHAT_PORT(6, "家长微信端"),
    PARENT_APP_PORT(7, "家长APP端");

    private final Integer id;
    private final String entranceName;

    public static final Map<Integer, AgentUsedProductType> productTypeMap;

    static {
        productTypeMap = new HashMap<>();
        for (AgentUsedProductType type : values()) {
            productTypeMap.put(type.getId(), type);
        }
    }

    public static AgentUsedProductType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return AgentUsedProductType.productTypeMap.get(id);
    }
}
