package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

/**
 * @author zhangbin
 * @since 2017/3/30 19:58
 */

public enum ScholarKeyType {
    REACHRATE(1, "单元达标率"),
    GOAL(2, "查看学情评估"),
    ASSIGN(3, "布置作业"),
    AUTHENTICATION(4, "老师认证");

    @Getter
    private Integer value;
    @Getter
    private String name;

    ScholarKeyType(Integer value, String name) {
        this.value = value;
        this.name = name;
    }
}