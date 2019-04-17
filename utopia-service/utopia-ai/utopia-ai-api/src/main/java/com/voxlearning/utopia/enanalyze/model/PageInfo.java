package com.voxlearning.utopia.enanalyze.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页参数
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo implements Serializable {
    /**
     * 多少页，从0开始
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;
}
