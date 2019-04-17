package com.voxlearning.enanalyze.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 字符识别结果
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticleOCRView implements Serializable {

    /**
     * 上传的图片记录流水
     */
    private String imageId;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 文本
     */
    private String text;
}
