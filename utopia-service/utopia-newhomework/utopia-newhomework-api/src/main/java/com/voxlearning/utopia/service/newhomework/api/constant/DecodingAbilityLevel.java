package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DecodingAbilityLevel {

    prepare("预备级", "会口头上的听音辨音和拼音；可以跟读", 0),
    level1("达到1级", "掌握26个字母的名称和发音，并能进行拼读；可以指读", 1),

    level2("达到2级", "能够拼读出常见的单音节单词（如cat，bag）；可以指读", 2),

    level3("达到3级", "掌握26个字母的名称和发音，并能进行拼读；可以指读", 3),

    level4("达到4级", "能够拼读出更复杂的单音节单词（如clock，shrimp）；可以开始较流利地朗读", 4),

    level5("达到5级", "能够拼读出双音节单词；可以流利朗读并自我纠错", 5),

    level6("达到6级", "能够拼读三音节及以上和词尾有变化的单词；可以流利有感情地朗读，开始自主默读", 6),

    level7("达到7级", "能够拼读出大部分符合拼读规则的单词；可以流利有感情地朗读，可以自主默读", 7);
    @Getter
    private final String desc;
    @Getter
    private final String detail;
    @Getter
    private final int level;

    DecodingAbilityLevel(String desc, String detail, int level) {
        this.desc = desc;
        this.detail = detail;
        this.level = level;
    }

    public static final Map<Integer, DecodingAbilityLevel> decodingAbilityLevelMap;

    static {
        decodingAbilityLevelMap = Stream.of(values()).collect(Collectors.toMap(DecodingAbilityLevel::getLevel, Function.identity()));
    }
}
