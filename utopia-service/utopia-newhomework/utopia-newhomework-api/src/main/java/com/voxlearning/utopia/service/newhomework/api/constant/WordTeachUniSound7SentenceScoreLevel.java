package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 语文字词讲练7分制
 * @Description: 云知声
 * @author: Mr_VanGogh
 * @date: 2018/12/17 下午5:03
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum WordTeachUniSound7SentenceScoreLevel {

    A("三颗星", 5, 7),
    B("二颗星", 3, 5),
    C("一颗星", 1, 3),
    D("没有星", 0, 1);

    @Getter
    private final String explain;
    @Getter private final int minScore;
    @Getter private final int maxScore;

    public static final List<Map<String, Object>> levels;

    static {
        levels = new ArrayList<>();
        for (WordTeachUniSound7SentenceScoreLevel level : values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("minScore", level.getMinScore());
            map.put("maxScore", level.getMaxScore());
            map.put("level", level.name());
            levels.add( map);
        }
    }
}
