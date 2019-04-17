package com.voxlearning.enanalyze.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 单独一页记录请求
 * @author xiaolei.li
 * @version 2018/7/6
 */
@Data
public class ArticleSingleRequest implements Serializable {

    /**
     * 作文记录id
     */
    private String articleId;
}
