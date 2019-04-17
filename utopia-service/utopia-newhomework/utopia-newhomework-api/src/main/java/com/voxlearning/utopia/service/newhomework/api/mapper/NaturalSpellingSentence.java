package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 应用跟读题和口语题 引擎打分结果
 * @author zhangbin
 * @since 2017/8/4
 */
@Getter
@Setter
@ToString
public class NaturalSpellingSentence implements Serializable {
    private static final long serialVersionUID = -8983239928842625445L;
    private String sample;                 // 评测传入文本
    private String usertext;               // 识别文本
    private Float begin;                   // 音频开始时间
    private Float end;                     // 音频结束时间
    private Double score;                  // 100分制成绩
    private Integer standardScore;         // 8分制成绩
    private String standardScoreLevel;     // 8分制成绩等级
    private Float fluency;                 // 流利度
    private Float integrity;               // 完整度
    private Float pronunciation;           // 标准度
    private List<Word> words;              // 单个字的相关信息

    @Getter
    @Setter
    @ToString
    public static class Word implements Serializable {
        private static final long serialVersionUID = 1782176282957271900L;
        private String text;                // 单字文本
        private Integer type;
        private Float score;                // 单字得分
        private List<SubWord> subwords;

        @Getter
        @Setter
        @ToString
        public static class SubWord implements Serializable {
            private static final long serialVersionUID = 7509133419204019920L;
            private String subtext;             // 文本
            private Integer toneofphone;        // 声母 toneofphone 字段全部输出-1；韵母的声调对错，0:错误;1:正确
            private Float score;                // 声母得分/韵母得分
        }
    }
}