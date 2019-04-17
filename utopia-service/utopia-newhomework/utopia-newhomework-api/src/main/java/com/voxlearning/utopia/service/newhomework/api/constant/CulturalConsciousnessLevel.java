package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum CulturalConsciousnessLevel {

    prepare("预备级", "知道外国国家和外国人士的存在", 0),

    level1("达到1级", "主要涉及以下主题：饮食、娱乐、爱好、学校、课堂、家人亲友主题", 1),

    level2("达到2级", "主要涉及以下主题：饮食、娱乐、爱好、学校、课堂、家人亲友主题", 2),

    level3("达到3级", "主要涉及以下主题：习俗文化、体育赛事、音乐舞蹈、电影戏剧、节日庆祝、家庭学校", 3),

    level4("达到4级", "主要涉及以下主题：习俗文化、体育赛事、音乐舞蹈、电影戏剧、节日庆祝、家庭学校", 4),

    level5("达到5级", "主要涉及以下主题：诗歌、故事、名人、历史、科普、名胜古迹、城市国家", 5),

    level6("达到6级", "主要涉及以下主题：诗歌、故事、名人、历史、科普、名胜古迹、城市国家", 6),

    level7("达到7级", "主要涉及以下主题：科普、西方经典文学（简写版）、家庭关系等", 7);
    @Getter
    private final String desc;
    @Getter
    private final String detail;
    @Getter
    private final int level;

    CulturalConsciousnessLevel(String desc, String detail, int level) {
        this.desc = desc;
        this.detail = detail;
        this.level = level;
    }
    public static final Map<Integer, CulturalConsciousnessLevel> culturalConsciousnessLevelMap;

    static {
        culturalConsciousnessLevelMap = Stream.of(values()).collect(Collectors.toMap(CulturalConsciousnessLevel::getLevel, Function.identity()));
    }
}
