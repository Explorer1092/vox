package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务反馈类型枚举类
 * @author deliang.che
 * @date 2018-05-28
 */
@Getter
public enum AgentTaskFeedbackType {


    TEL("打电话"),
    WECHAT_OR_QQ("微信/QQ联系"),
    SMS("发短信"),
    FTF_CONTACT("当面沟通");

    public final String value;

    private static Map<String, AgentTaskFeedbackType> descMap = new HashMap<>();
    static {
        for (AgentTaskFeedbackType item : AgentTaskFeedbackType.values()) {
            descMap.put(item.getValue(), item);
        }
    }

    AgentTaskFeedbackType(String value) {
        this.value = value;
    }

    public static AgentTaskFeedbackType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static AgentTaskFeedbackType descOf(String name){
        return descMap.get(name);
    }

}
