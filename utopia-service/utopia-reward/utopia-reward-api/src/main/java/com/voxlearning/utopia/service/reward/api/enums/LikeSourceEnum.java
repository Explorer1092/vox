package com.voxlearning.utopia.service.reward.api.enums;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public enum LikeSourceEnum {
    A17("17作业"),
    QQ("QQ"),
    WECHAT("微信"),
    WEB("网页");

    @Getter
    @Setter
    private String name;

    LikeSourceEnum(String name) {
        this.name = name;
    }

    public static LikeSourceEnum safeValueOf(String name) {
        for (LikeSourceEnum likeSourceEnum : LikeSourceEnum.values()) {
            if (Objects.equals(name, likeSourceEnum.name())) {
                return likeSourceEnum;
            }
        }
        return null;
    }
}
