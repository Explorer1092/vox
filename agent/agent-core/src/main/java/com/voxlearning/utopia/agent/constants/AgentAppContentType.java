package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 市场APP的可更新内容的枚举
 * Created by Administrator on 2016/8/2.
 */
@Getter
@RequiredArgsConstructor
public enum AgentAppContentType {
    MARKETING_ACTIVITY(1, "市场活动"),
    UPDATE_LOG(2, "平台更新日志"),
    RECOMMEND_BOOK(3, "推荐书籍"),
    DATA_PACKET(4, "资料包");

    private final Integer id;
    private final String typeName;

    private static final Map<Integer, AgentAppContentType> contentTypeMap;

    static {
        contentTypeMap = new HashMap<>();
        for (AgentAppContentType type : values()) {
            contentTypeMap.put(type.getId(), type);
        }
    }

    public static AgentAppContentType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return AgentAppContentType.contentTypeMap.get(id);
    }
}
