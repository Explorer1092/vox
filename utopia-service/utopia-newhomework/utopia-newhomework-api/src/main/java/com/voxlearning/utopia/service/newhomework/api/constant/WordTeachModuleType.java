package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

/**
 * @Description: 字词讲练：模块类型
 * @author: Mr_VanGogh
 * @date: 2018/12/11 下午4:55
 */
public enum WordTeachModuleType {

    WORDEXERCISE(1, "字词训练"),
    IMAGETEXTRHYME(2, "图文入韵"),
    CHINESECHARACTERCULTURE(3, "汉字文化");

    @Getter
    private Integer value;
    @Getter
    private String name;

    WordTeachModuleType(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public static WordTeachModuleType of(String type) {
        try {
            return WordTeachModuleType.valueOf(type);
        } catch (Exception ignored) {
            return null;
        }
    }

}
