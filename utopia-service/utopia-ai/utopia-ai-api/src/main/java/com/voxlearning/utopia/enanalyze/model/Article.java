package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 完整的作业批改记录
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class Article implements Serializable {

    /**
     * openid
     */
    private String openId;

    /**
     * 作文记录流水
     */
    private String articleId;

    /**
     * 作文
     */
    private String text;

    /**
     * 超越百分比
     */
    private int beyondRate;

    /**
     * 总体评价
     */
    private String evaluation;

    /**
     * 次要评价
     */
    private String subEvaluation;

    /**
     * 语法检查部分
     */
    private SyntaxCheckDetail[] syntaxCheckDetails;

    /**
     * 好句
     */
    private String[] goodSents;

    /**
     * 好词
     */
    private String[] goodWords;

    /**
     * 作文基本能力
     */
    private ArticleBasicAbility basicAbility;

    /**
     * 作文综合能力
     */
    private ArticleCompositeAbility compositeAbility;

    /**
     * 语法检查项目
     */
    @Data
    public static class SyntaxCheckDetail implements Serializable {
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
