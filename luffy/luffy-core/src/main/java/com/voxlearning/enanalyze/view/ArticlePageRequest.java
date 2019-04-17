package com.voxlearning.enanalyze.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 批改记录分页查询请求
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticlePageRequest implements Serializable {

    /**
     * 第几页
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

}
