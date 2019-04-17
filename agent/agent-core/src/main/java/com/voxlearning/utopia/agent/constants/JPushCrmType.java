package com.voxlearning.utopia.agent.constants;

/**
 * Created by xianlong.zhang
 */
public enum JPushCrmType {
    AGENT_ROLE("agent_role_"),
    AGENT_GROUP("agent_group_");
    public String tagName;

    JPushCrmType(String tagName) {
        this.tagName = tagName;
    }
    public String generateTag(Object tagValue) {
        return this.tagName + tagValue.toString();
    }
}
