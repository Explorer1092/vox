package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AI问题语音引擎评测结果
 * @author songtao
 * @since 2018/4/16
 */
@Getter
@Setter
public class AIQuestionAppraisionRequest implements Serializable {
    private static final long serialVersionUID = -4448646361243307150L;
    private String code;
    private String message;
    private String voiceURI;
    private BigDecimal score;
    private List<Line> lines;

    @Getter
    @Setter
    public static class Line implements Serializable {
        private static final long serialVersionUID = 0L;
        private String sample;
        private BigDecimal standardScore;
        private BigDecimal sentStandardScore;
        private BigDecimal score;
        private BigDecimal businessLevel;
        private BigDecimal integrity;
        private BigDecimal pronunciation;
        private String usertext;
        private List<Word> words;
        private BigDecimal fluency;
        private BigDecimal end;

    }

    @Getter
    @Setter
    public static class Word implements Serializable {
        private static final long serialVersionUID = 0L;
        private String phonetic;
        private String text;
        private BigDecimal score;
        private List<SubWord> subwords;
    }

    @Getter
    @Setter
    public static class SubWord implements Serializable {
        private static final long serialVersionUID = 0L;
        private String subtext;
        private BigDecimal score;
    }

}
