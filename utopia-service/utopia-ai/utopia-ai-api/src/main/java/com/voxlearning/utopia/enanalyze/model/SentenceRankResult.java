package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 结果 - 群排名
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class SentenceRankResult implements Serializable {

    /**
     * 群id
     */
    private String openGroupId;

    /**
     * openid
     */
    private String openId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像url
     */
    private String avatarUrl;

    /**
     * 最好的句子
     */
    private String sentence;

    /**
     * 句子得分
     */
    private float sentenceScore;

    /**
     * 排名
     */
    private long rank;

    /**
     * 点赞数
     */
    private long likes;

    /**
     * 当前用户是否对其点赞
     */
    private boolean likeStatus;
}
