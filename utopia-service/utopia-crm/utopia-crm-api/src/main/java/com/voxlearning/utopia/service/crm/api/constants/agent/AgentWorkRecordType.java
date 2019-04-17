package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.Getter;

/**
 * @author deliang.che
 * @since 2019/1/18
 */
@Getter
public enum AgentWorkRecordType {
    SCHOOL("进校"),
    MEETING("组会"),
    RESOURCE_EXTENSION("资源拓维"),
    ACCOMPANY("陪同");

    public final String desc;

    AgentWorkRecordType(String desc) {
        this.desc = desc;
    }

    public static AgentWorkRecordType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
