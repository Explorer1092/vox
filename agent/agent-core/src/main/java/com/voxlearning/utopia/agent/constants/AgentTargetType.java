package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 *
 * @author song.wang
 * @date 2017/5/23
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentTargetType {
    NOTIFIY(1, "通知"),
    TEACHER(2, "老师")
    ;

    private final int status;
    private final String desc;

}
