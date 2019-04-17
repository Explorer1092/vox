package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ReadingHabitsLevel {
    prepare("预备级", "每周听读1本本级别难度绘本，累计阅读词数达到200", 0),
    level1("达到1级", "每周阅读至少2本本级别难度及以下的绘本，累计阅读词数达到1000", 1),

    level2("达到2级", "每周阅读至少2本本级别及以下的难度绘本，累计阅读词数达到2000", 2),

    level3("达到3级", "每周阅读至少3本本级别难度及以下的绘本，累计阅读词数达到5000", 3),

    level4("达到4级", "每周阅读至少3本本级别难度及以下的绘本，累计阅读词数达到12000", 4),

    level5("达到5级", "每周阅读至少3本本级别难度及以下的绘本，累计阅读词数达到23000", 5),

    level6("达到6级", "每周阅读至少3本本级别难度及以下的绘本，累计阅读词数达到40000", 6),

    level7("达到7级", "每周阅读至少3本本级别难度及以下的绘本，累计阅读词数达到70000", 7);
    @Getter
    private final String desc;
    @Getter
    private final String detail;
    @Getter
    private final int level;

    ReadingHabitsLevel(String desc, String detail, int level) {
        this.desc = desc;
        this.detail = detail;
        this.level = level;
    }

    public static final Map<Integer, ReadingHabitsLevel> readingHabitsLevelMap;

    static {
        readingHabitsLevelMap = Stream.of(values()).collect(Collectors.toMap(ReadingHabitsLevel::getLevel, Function.identity()));
    }
}
