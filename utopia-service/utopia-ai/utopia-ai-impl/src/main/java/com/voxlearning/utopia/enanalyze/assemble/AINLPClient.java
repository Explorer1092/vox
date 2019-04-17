package com.voxlearning.utopia.enanalyze.assemble;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * ai组的语义分析客户端
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@FunctionalInterface
public interface AINLPClient {

    /**
     * 语义分析
     *
     * @param text 英文作文
     * @return 语义分析结果
     */
    Result nlp(String text);

    /**
     * 语义分析结果
     */
    @Data
    class Result implements Serializable {
        private String code;
        private String message;
        private String version;
        private SyntaxCheck syntaxCheck;
        private EssayRating essayRating;

        /**
         * nlp结果
         */
        @Data
        public static class SyntaxCheck implements Serializable {
            private String code;
            private List<Content> content;

            @Data
            public static class Content implements Serializable {
                private float score;
                private String original;
                private List<Detail> details;

                @Data
                public static class Detail implements Serializable {
                    private String text;
                    private String answer;
                    private String description;
                    private String type;
                    private int sentIndex;
                    private int sentBegin;
                    private int sentEnd;
                    private int start;
                    private int end;
                    private boolean modified;
                }
            }
        }

        @Data
        public static class EssayRating implements Serializable {
            private int essay_length;
            private float overall_score;
            private float lexical_score;
            private float grammar_score;
            private float sentence_score;
            private float structure_score;
            private float content_score;
            private GoodWord[] good_words;
            private GoodSent[] good_sents;

            @Data
            public static class GoodWord implements Serializable {
                private String word;
                private float score;
            }

            @Data
            public static class GoodSent implements Serializable {
                private String sentence;
                private float score;
            }
        }
    }

    @AllArgsConstructor
    enum SyntaxCheckType {
        SPL("拼写错误", "spelling"),
        CAP("大小写错误", "capitalization"),
        PUN("标点错误，标点缺失", "punctuation"),
        MIS("单词缺失", "missing word"),
        VTE("动词时态错误", "verb tense error"),
        VM("动词错误", "/"),
        SVA("主语动词一致", "subject-verb-agreenet"),
        POW("用词不够好", "poor diction"),
        CON("连词错误", "conjunction"),
        PRE("介词错误", "preposition"),
        ADP("副词错误", "adverb / adjective position"),
        TYP("输入错误", "type error"),
        Smod("悬垂修饰语", "dangling modifier"),
        CUT("应该删除的错误", "cut"),
        PRO("代词错误", "pronoun"),
        ART("冠词错误", "article"),
        WFO("词汇形态错误", "word form"),
        FRG("不完整句子", "fragile"),
        STM("句子层面错误", "/"),
        POS("所有格错误", "possessive"),
        CNT("否定形式错误", "contraction"),
        COU("单复数错误", "countable"),
        GND("性别词使用错误", "gender"),
        COL("连写错误，搭配错误", "contracted"),
        ABV("缩写错误", "abbreviation"),
        VOI("用词错误", "improper word use"),
        ADJ("形容词错误", "adjective"),
        MOD("比较级错误", "comparative error"),
        CW("常用易混词", "commonly confused words"),
        GRA("语法错误", "/"),
        MISC("其他错误", "/"),
        MTEP("术语滥用", "/"),
        NSP("不标准短语", "/"),
        PE("可以简化的英语", "/"),
        RED("冗余短语", "/"),
        SEM("语义错误", "/"),
        STY("风格问题", "/"),
        TG("排版错误", "/"),
        Cit("引文错误", "/"),
        WIKI("维基百科建议的修正", "/"),
        UNKNOWN("未知类型", "UNKNOWN");;

        /**
         * 描述中文
         */
        public final String DESC_CN;
        public final String DESC_EN;

        /**
         * 根据名称获取枚举
         *
         * @param type 名称
         * @return 枚举
         */
        public static SyntaxCheckType of(String type) {
            return Arrays.stream(SyntaxCheckType.values())
                    .filter(i -> i.name().equals(type))
                    .findFirst().orElse(UNKNOWN);
        }
    }

}
