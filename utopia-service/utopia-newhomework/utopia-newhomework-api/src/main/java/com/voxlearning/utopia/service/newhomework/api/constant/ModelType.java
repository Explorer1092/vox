package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 古诗模块类型枚举
 * @author majianxin
 * @version V1.0
 * @date 2019/2/25
 */
@AllArgsConstructor
public enum ModelType {
    EXPOUND(1, "名师讲解"),
    APPRECIATE(2, "名句赏析"),
    RECITE(3, "每日朗诵"),
    FUN(4, "巩固练习");

    @Getter
    private Integer value;
    @Getter
    private String desc;

    public static ModelType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
