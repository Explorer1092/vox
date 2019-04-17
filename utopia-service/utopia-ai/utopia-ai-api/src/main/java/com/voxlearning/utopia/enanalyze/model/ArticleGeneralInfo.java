package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 作文批改记录摘要信息
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticleGeneralInfo implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * 原文
     */
    private String text;

    /**
     * 创建时间
     */
    private Date createDate;
}
