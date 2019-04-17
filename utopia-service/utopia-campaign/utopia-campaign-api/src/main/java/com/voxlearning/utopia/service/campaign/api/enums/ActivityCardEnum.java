package com.voxlearning.utopia.service.campaign.api.enums;

import lombok.Getter;

@Getter
public enum ActivityCardEnum {

    guan("观"),
    hai("海"),
    de("得"),
    shen("深"),
    zhan("瞻"),
    tian("天"),
    jian("见"),
    da("大");

    private String desc;

    ActivityCardEnum(String desc) {
        this.desc = desc;
    }
}