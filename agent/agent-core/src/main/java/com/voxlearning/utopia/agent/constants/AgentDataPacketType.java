package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 市场App资料包类型
 * Created by yaguang.wang on 2016/8/2.
 */
@Getter
@RequiredArgsConstructor
public enum AgentDataPacketType {

    POLICY_PAPER(1, "政策文件"),
    NEW_STAFF_MUST_READER(2, "新员工必读"),
    EXPLANATION_NOUNS(3, "名词解释"),
    LEADER_INFO(4, "接口人信息"),
    UTIl_AND_WORK_STREAM(5, "工具和流程使用"),
    PRODUCT_FAQ(6, "产品FAQ"),
    TASK_APPLY_DATA(7, "课题申请资料");

    public static final Map<Integer, AgentDataPacketType> datePacketType;

    static {
        datePacketType = new HashMap<>();
        for (AgentDataPacketType type : values()) {
            datePacketType.put(type.getId(), type);
        }
    }

    private final Integer id;
    private final String desc;

    public static AgentDataPacketType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return AgentDataPacketType.datePacketType.get(id);
    }
}
