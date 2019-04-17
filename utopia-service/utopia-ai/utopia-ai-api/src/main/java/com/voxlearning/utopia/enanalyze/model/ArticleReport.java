package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学情报告
 *
 * @author xiaolei.li
 * @version 2018/7/19
 */
@Data
public class ArticleReport implements Serializable {

    /**
     * openid
     */
    private String openId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * tag
     */
    private String tag;

    /**
     * 超越了多少人（百分比，[0,100])
     */
    private int beyondRate;

    /**
     * 总体评价
     */
    private String evaluation;

    /**
     * 能力模型
     */
    private ArticleCompositeAbility ability;

    /**
     * 能力评价
     */
    private String abilityEvaluation;

    /**
     * 批改记录
     */
    private Record[] records;

    /**
     * 历史成绩变化评价
     */
    private String recordEvaluation;


    /**
     * 批改记录
     */
    @Data
    public static class Record implements Serializable {

        /**
         * 创建时间
         */
        private Date createDate;

        /**
         * 词法评分
         */
        private float lexicalScore;

        /**
         * 句法评分
         */
        private float sentenceScore;

        /**
         * 内容评分
         */
        private float contentScore;

        /**
         * 结构评分
         */
        private float structureScore;
    }
}
