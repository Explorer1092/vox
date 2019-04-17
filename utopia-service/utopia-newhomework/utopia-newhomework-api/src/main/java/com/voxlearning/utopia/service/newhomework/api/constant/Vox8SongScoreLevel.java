package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 自研引擎歌曲打分(8分制)
 * @author: Mr_VanGogh
 * @date: 2018/9/13 下午3:04
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Vox8SongScoreLevel {

    A("三颗星", 2, 8),
    D("没有星", 0, 1);

    @Getter
    private final String explain;
    @Getter private final int minScore;
    @Getter private final int maxScore;

    public static final List<Map<String, Object>> levels;

    static {
        levels = new ArrayList<>();
        for (Vox8SongScoreLevel level : values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("minScore", level.getMinScore());
            map.put("maxScore", level.getMaxScore());
            map.put("level", level.name());
            levels.add( map);
        }
    }

    public static Vox8SongScoreLevel processLevel(Double score){
        if(score != null){
            for (Vox8SongScoreLevel level : values()) {
                if(score >= level.getMinScore() && score<= level.getMaxScore()){
                    return level;
                }
            }
        }
        return D;
    }
}
