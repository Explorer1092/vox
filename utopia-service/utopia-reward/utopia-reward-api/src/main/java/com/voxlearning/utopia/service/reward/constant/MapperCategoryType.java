package com.voxlearning.utopia.service.reward.constant;

import lombok.Getter;

/**
 * @description: 前端接口使用，分类集合和一级分类区分类型
 * @author: kaibo.he
 * @create: 2018-11-06 16:52
 **/
public enum  MapperCategoryType {
    CATEGORY(1, "一级分类"),
    SET(2, "分类集合"),
    ;

    private Integer type;
    @Getter
    private String name;
    MapperCategoryType(Integer type, String name) {
        this.name = name;
        this.type = type;
    }

    public Integer intType() {
        return type;
    }
}
