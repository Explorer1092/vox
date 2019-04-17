package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 请求参数 - 作文批改
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticleNLPParams implements Serializable {

    /**
     * openid
     */
    private String openId;

    /**
     * 批改记录id
     */
    private String articleId;

    /**
     * 作文文本
     */
    private String text;
}
