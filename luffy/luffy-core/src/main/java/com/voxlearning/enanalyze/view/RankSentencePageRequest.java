package com.voxlearning.enanalyze.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 排行榜分页查询条件
 *
 * @author xiaolei.li
 * @version 2018/7/6
 */
@Data
public class RankSentencePageRequest implements Serializable {

    /**
     * 第几页
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 组id
     */
    private String openGroupId;
}
