package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;

/**
 * ocr识别结果
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticleOCRResult implements Serializable {

    /**
     * 文件id
     */
    private String imageId;

    /**
     * 文本
     */
    private String text;
}
