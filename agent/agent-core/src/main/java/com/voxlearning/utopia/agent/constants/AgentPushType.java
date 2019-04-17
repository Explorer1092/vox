package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

/**
 * AgentPushType
 *
 * @author song.wang
 * @date 2018/9/12
 */
public enum AgentPushType {

    NEW_MESSAGE("您有新消息！");

    @Getter
    private final String desc;

    AgentPushType(String desc){
        this.desc = desc;
    }
}
