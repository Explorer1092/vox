package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum WorkSheetEvent {
    UNDEFINED(-1,"未定义"),
    CREATE(0,"创建工单"),
    REPLY(1,"回复工单"),
    FINISH(2,"完结工单"),
    TRANSMIT(3,"转交工单"),
    MODIFY(4,"修改工单"),
    APPLY(5,"申领(分配)工单"),
    AUTO_APPLY(6,"系统分配工单"),
    REBORN(7,"重新发起"),
    APPROVE(8,"批准工单"),
    REJECT(9,"驳回工单"),
    REOPEN(10,"重新发起工单");
    public final int typeId;
    public final String desc;

    public static final Map<Integer, WorkSheetEvent> eventMap;

    static {
        eventMap = new HashMap<>();
        for (WorkSheetEvent event : values()) {
            eventMap.put(event.getTypeId(), event);
        }
    }
}
