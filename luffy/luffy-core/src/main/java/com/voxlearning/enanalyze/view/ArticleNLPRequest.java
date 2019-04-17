package com.voxlearning.enanalyze.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 作文分析请求
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticleNLPRequest implements Serializable {

    private String articleId;

    /**
     * 作文
     */
    private String text;
}
