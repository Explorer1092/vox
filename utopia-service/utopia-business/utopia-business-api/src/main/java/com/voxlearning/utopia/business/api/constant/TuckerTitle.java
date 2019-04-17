package com.voxlearning.utopia.business.api.constant;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public enum TuckerTitle {
    Unknown(0, "暂无称号", "您没有完成目标，还没有获得任何称号～"),
    Lv1(1, "小有所成", "好的开始是成功的一半，继续坚持吧！"),
    Lv2(2, "初见成效", "和学生一起见证坚持的力量，向好习惯迈进！"),
    Lv3(3, "习惯养成", "21天养成一个好习惯，多年后学生一定会感激您！"),
    Lv4(4, "教学达人", "行百里者半九十，您离最后的巅峰只有一步之遥啦，继续加油吧！"),
    Lv5(5, "教学精英", " 恭喜您成功登顶，科技助力，让教学更高效！"),
    ;

    @Getter private final Integer level;
    @Getter private final String title;
    @Getter private final String description;

    TuckerTitle(int level, String title, String description) {
        this.level = level;
        this.title = title;
        this.description = description;
    }

    private static final Map<Integer, TuckerTitle> map;

    static {
        map = new LinkedHashMap<>();
        for (TuckerTitle title : values()) {
            map.put(title.getLevel(), title);
        }
    }

    public static TuckerTitle parse(Integer level) {
        return map.getOrDefault(level, Unknown);
    }
}