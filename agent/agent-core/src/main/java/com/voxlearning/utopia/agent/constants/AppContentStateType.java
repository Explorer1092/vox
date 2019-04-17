package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * app 内容维护的状态
 * Created by yaguang.wang on 2016/8/11.
 */
@Getter
@RequiredArgsConstructor
public enum AppContentStateType {
    TEMPORARY_STORAGE(1, "暂存"),
    RELEASE(2, "发布");

    private final Integer stateCode;
    private final String desc;

    private static final Map<Integer, AppContentStateType> stateTypeMap;

    static {
        stateTypeMap = new HashMap<>();
        for (AppContentStateType type : values()) {
            stateTypeMap.put(type.getStateCode(), type);
        }
    }

    public static AppContentStateType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return AppContentStateType.stateTypeMap.get(id);
    }
}
