package com.voxlearning.utopia.enanalyze.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 作文得分排名实体
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticleScoreEntity implements Serializable {

    /**
     * 作文记录id
     */
    private String articleId;

    /**
     * 用户id
     */
    private String openId;

    /**
     * 作文得分
     */
    private double score;

    /**
     * 时间戳，用于得分相同排名相同的决策条件，时间戳越小，排名靠前
     */
    private long timestamp;

}
