package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PhoneticKnowledgeLevel {

    prepare("预备级", "知道单词的概念，可以识别英语单词和句子", 0),

    level1("达到1级", "认识并理解30-50个高频词和简单句", 1),

    level2("达到2级", "认识并理解80-100个高频词和简单句", 2),

    level3("达到3级", "运用高频词，学会分析复合词，认识4个屈折词尾尾缀（-s，-es，表复数，-‘s表所属，-ing）；认识稍长的简单句和and引导并列句", 3),

    level4("达到4级", "运用高频词，学会分析复合词、词根，认识4个屈折词尾尾缀（-er表比较，-est，-ed，-en）；认识稍长的简单句和and等引导的并列句；识别故事的开头、发展、结尾", 4),

    level5("达到5级", "运用词根词缀、复合词等策略判断词义；理解并列句和复合句", 5),

    level6("达到6级", "综合运用词根词缀、复合词、上下文等策略判断词义和判断多义词词义；理解并列句和复合句；识别故事和非故事类的主要文体", 6),

    level7("达到7级", "综合运用构词法、上下文、工具书等策略判断词义和判断多义词词义；理解多种并列句和简单复合句；辨识常见语篇结构", 7),;

    @Getter
    private final String desc;
    @Getter
    private final String detail;
    @Getter
    private final int level;

    PhoneticKnowledgeLevel(String desc, String detail, int level) {
        this.desc = desc;
        this.detail = detail;
        this.level = level;
    }

    public static final Map<Integer, PhoneticKnowledgeLevel> phoneticKnowledgeLevelMap;

    static {
        phoneticKnowledgeLevelMap = Stream.of(values()).collect(Collectors.toMap(PhoneticKnowledgeLevel::getLevel, Function.identity()));
    }
}
