package com.voxlearning.utopia.service.workflow.api.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流处理状态
 *
 * @author song.wang
 * @date 2017/1/5
 */
@Getter
public enum WorkFlowProcessResult {
    agree(1, "同意"),  // 同意 ， 为兼容老数据，agree为小写
    reject(2, "驳回"),
    raiseup(3, "转发"),
    revoke(4, "撤销");

    private final Integer type;
    private final String desc;

    WorkFlowProcessResult(Integer type,String desc){
        this.type = type;
        this.desc = desc;
    }

    private static final Map<Integer, WorkFlowProcessResult> processResultMap;

    static {
        processResultMap = new HashMap<>();
        for (WorkFlowProcessResult type : values()) {
            processResultMap.put(type.getType(), type);
        }
    }

    public static WorkFlowProcessResult typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return processResultMap.get(id);
    }

    public static WorkFlowProcessResult nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
