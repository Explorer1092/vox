package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 带有排名和点赞次数的群信息
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class GroupWithLike implements Serializable {

    /**
     * 群id
     */
    private String openGroupId;

    /**
     * 群名称
     */
    private String openGroupName;

    /**
     * openid
     */
    private String openId;

    /**
     * 排名
     */
    private long rank;

    /**
     * 点赞数
     */
    private long likes;
}
