package com.voxlearning.utopia.enanalyze.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 作文基本能力模型
 *
 * @author xiaolei.li
 * @version 2018/7/20
 */
@Data
public class ArticleBasicAbility implements Serializable {

    /**
     * 能力总分
     */
    private Float score;

    /**
     * 词法得分
     */
    private Float lexicalScore;

    /**
     * 句法得分
     */
    private Float sentenceScore;

    /**
     * 内容得分
     */
    private Float contentScore;

    /**
     * 结构得分
     */
    private Float structureScore;

    /**
     * 评分维度
     */
    @AllArgsConstructor
    public enum Dimension {
        GRAMMAR("语法"),
        LEXICAL("此法"),
        SENTENCE("句法"),
        STRUCTURE("结构"),
        CONTENT("内容"),;
        public final String DESC;
    }

}
