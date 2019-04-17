package com.voxlearning.utopia.service.crm.api.constants;

import lombok.Getter;

/**
 *  平台类型
 *
 * @author song.wang
 * @date 2017/1/4
 */
@Getter
public enum SystemPlatformType {
    UNKNOWN("未知"),
    AGENT("市场"),
    ADMIN("CRM");

    private final String desc;
    SystemPlatformType(String desc){
        this.desc = desc;
    }

    public static SystemPlatformType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
