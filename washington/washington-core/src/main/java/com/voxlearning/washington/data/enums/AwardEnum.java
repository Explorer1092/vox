package com.voxlearning.washington.data.enums;

import lombok.AllArgsConstructor;

/**
 * 奖项级别枚举
 *
 * @Author: peng.zhang
 * @Date: 2018/10/22
 */
@AllArgsConstructor
public enum AwardEnum {

    NATIONAL_LEVEL(11,"国家级"),
    PROVINCIAL_LEVEL(12,"省级"),
    CITY_LEVEL(13,"市级"),
    SCHOOL_LEVEL(14,"校级"),
    EMPTY(15,"无");

    public Integer code;
    public String desc;
}
