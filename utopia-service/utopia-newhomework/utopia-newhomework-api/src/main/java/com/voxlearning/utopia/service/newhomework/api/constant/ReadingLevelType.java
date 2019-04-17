package com.voxlearning.utopia.service.newhomework.api.constant;

import com.voxlearning.alps.core.util.MapUtils;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ReadingLevelType {
    L0A("预备级", "", 0),
    L1A("L1", "A", 1),
    L1B("L1", "B", 2),
    L2A("L2", "A", 3),
    L2B("L2", "B", 4),
    L3A("L3", "A", 5),
    L3B("L3", "B", 6),
    L4A("L4", "A", 7),
    L4B("L4", "B", 8),
    L5A("L5", "A", 9),
    L5B("L5", "B", 10),
    L6A("L6", "A", 11),
    L6B("L6", "B", 12),
    L7A("L7", "A", 13),
    L7B("L7", "B", 14);
    @Getter
    private final String desc;
    @Getter
    private final String detail;
    @Getter
    private final int level;


    public static List<Map<String, Object>> detailInfo = new LinkedList<>();


    ReadingLevelType(String desc, String detail, int level) {
        this.desc = desc;
        this.detail = detail;
        this.level = level;
    }

    public static final Map<Integer, ReadingLevelType> readingLevelTypeMap;

    static {
        readingLevelTypeMap = Stream.of(values()).collect(Collectors.toMap(ReadingLevelType::getLevel, Function.identity()));
        for (ReadingLevelType readingLevelType : ReadingLevelType.readingLevelTypeMap.values()) {
            detailInfo.add(MapUtils.m(
                    "desc", readingLevelType.getDesc(),
                    "detail", readingLevelType.getDetail(),
                    "level", readingLevelType.getLevel()
            ));
        }

    }
}
