package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum IntelligenceExamSceneType {
    NONE(0, "综合题"),
    MATH_EXAMPLE(1, "课后习题相似"),
    MATH_VARIANT(2, "基础巩固"),
    MATH_CRUX(3, "拓展提升"),
    MATH_WEAK(4, "查缺补漏"),
    MATH_LABEL_NUM_AND_ALGEBRA(9, "数与代数"),
    MATH_LABEL_SPACE_AND_GRAPHICS(10, "空间与图形"),
    MATH_LABEL_STATISTICS_AND_PROBABILITY(11, "统计与概率"),
    MATH_LABEL_SYNTHESIS_AND_APPLICATIONS(12, "综合及应用"),

    ENGLISH_VOCABULARY(13, "词汇专项"),
    ENGLISH_GRAMMAR(14, "句型语法专项"),
    ENGLISH_LESSON(15, "情景综合"),
    ENGLISH_LISTENING(16, "听力专项"),
    ENGLISH_UNIT_REVIEW_VOCABULARY(17, "词汇专项"),
    ENGLISH_UNIT_REVIEW_GRAMMAR(18, "句型语法专项"),
    ENGLISH_UNIT_REVIEW_LESSON(19, "情景综合"),

    MATH_UNIT_REIVEW_REQUIRED_SKILL(20, "应知应会"),
    MATH_UNIT_REIVEW_DEVELOPMENT_MIGRATION(21, "拓展迁移"),
    MATH_LOCAL_SPECIALIZED_VERSION(22, "本地专版"),
    MATH_STEP_DISASSEMBLY(23, "步骤拆解"),

    ENGLISH_ALPHABET_PRONOUNCE(24, "字母语音"),
    ENGLISH_ORAL_PRACTICE(25, "口语专项"),
    ENGLISH_OTHER_RELATED(26, "其他相关知识点"),

    CHINESE_NEW_WORD(27, "生字专项"),
    CHINESE_KEY_WORD(28, "词语专项"),
    CHINESE_ACCUMULATION_USE(29, "积累运用"),

    CHINESE_WORD_BASE(34, "字词基础"),
    CHINESE_WORD_ENHANCE(35, "字词拔高"),
    CHINESE_SENTENCE(36, "句子专项"),
    CHINESE_GRAMMATICAL_RHETORIC(37, "语法修辞"),
    CHINESE_DICTATION(38, "听写"),
    CHINESE_SPECIAL_WORDS(40, "生字专项"),
    CHINESE_SPECIAL_VOCABULARY(41, "词汇专项"),
    CHINESE_WORD_BASE_NEW(42, "字词基础"),
    CHINESE_WORD_ENHANCE_NEW(43, "字词拔高"),
    CHINESE_EXPAND_USE(44, "拓展运用"),

    ENGLISH_READING(45, "阅读专项");

    @Getter private final int id;
    @Getter private final String name;

    public static IntelligenceExamSceneType of(int id) {
        for (IntelligenceExamSceneType t : values()) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public static IntelligenceExamSceneType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ignored) {
            return null;
        }
    }


}
