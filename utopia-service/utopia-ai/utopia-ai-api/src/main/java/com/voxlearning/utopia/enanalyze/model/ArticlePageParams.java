package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 批改记录分页查询条件
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticlePageParams implements Serializable {
    private String openId;
    private int page;
    private int size;
}
