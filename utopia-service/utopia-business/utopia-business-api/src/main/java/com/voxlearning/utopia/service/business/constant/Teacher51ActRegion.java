package com.voxlearning.utopia.service.business.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 老师五一活动地区
 */
@Getter
@AllArgsConstructor
public enum Teacher51ActRegion {
    Tch51ActDefaultAward(3,30),   // 默认档
    Tch51ActHighAward(3,50);      // 高档位

    private int condition;
    private int awardMoney;

}
