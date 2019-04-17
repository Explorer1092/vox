package com.voxlearning.utopia.service.reward.constant;

import lombok.Getter;

/**
 * @description: 一级分类类型
 * @author: kaibo.he
 * @create: 2018-10-26 18:54
 **/
public enum TwoLevelCategoryType {
    TOBY_IMG(1, "形象"),
    TOBY_COUNTENANCE(2, "表情"),
    TOBY_PROPS(3, "道具"),
    TOBY_ACCESSORY(4, "装饰"),
    ;

    private Integer type;
    private String name;
    TwoLevelCategoryType(Integer type, String name) {
        this.name = name;
        this.type = type;
    }

    public Integer intType() {
        return type;
    }
}
