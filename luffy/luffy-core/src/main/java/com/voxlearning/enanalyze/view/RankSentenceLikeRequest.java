package com.voxlearning.enanalyze.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 排行点赞请求
 *
 * @author xiaolei.li
 * @version 2018/7/6
 */
@Data
public class RankSentenceLikeRequest implements Serializable {

    /**
     * 群id
     */
    private String openGroupId;

    /**
     * 谁点的赞
     */
    private String fromOpenId;

    /**
     * 点给谁的赞
     */
    private String toOpenId;
}
