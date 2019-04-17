package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ReadingComprehensionLevel {

    prepare("预备级", "可以通过画面辅助看懂动画、歌谣等形式的英文材料", 0),

    level1("达到1级", "根据图片辅助理解文意，简单回忆故事并表达喜好", 1),

    level2("达到2级", "根据图片辅助理解文意，简单回忆故事并表达喜好", 2),

    level3("达到3级", "获取细节信息，进行推测，简单总结、复述故事", 3),

    level4("达到4级", "获取细节信息，进行推测，简单总结、复述故事", 4),

    level5("达到5级", "阅读故事和非故事类文本并运用对应的阅读策略", 5),

    level6("达到6级", "阅读故事和非故事类文本并运用对应的阅读策略", 6),

    level7("达到7级", "阅读故事和非故事类文本并运用对应的阅读策略，形成自主思考和判断", 7);
    @Getter
    private final String desc;
    @Getter
    private final String detail;
    @Getter
    private final int level;

    ReadingComprehensionLevel(String desc, String detail, int level) {
        this.desc = desc;
        this.detail = detail;
        this.level = level;
    }
    public static final Map<Integer, ReadingComprehensionLevel> readingComprehensionLevelMap;

    static {
        readingComprehensionLevelMap = Stream.of(values()).collect(Collectors.toMap(ReadingComprehensionLevel::getLevel, Function.identity()));
    }
}
