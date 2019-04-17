package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 自主练习-目标时长
 * @author: Mr_VanGogh
 * @date: 2018/4/18 下午8:41
 */
@AllArgsConstructor
public enum TargetTimeType {

    SmallStep(5L, "小步提升", 1),
    FastImprove(10L, "快速提高", 2),
    AdvancedSuper(20L, "进阶学霸", 3);

    @Getter private final Long time;
    @Getter private final String text;
    @Getter private final Integer difficult;

    public static TargetTimeType of(String text) {
        try {
            return valueOf(text);
        } catch (Exception ex) {
            return null;
        }
    }

    public static TargetTimeType of(Long time) {
        for (TargetTimeType t : values()) {
            if (time.equals(t.getTime())) {
                return t;
            }
        }
        return null;
    }
}
