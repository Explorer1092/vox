package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批改记录分页输出
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticlePageResult implements Serializable {
    List<ArticleGeneralInfo> data;
}
