package com.voxlearning.utopia.admin.productpromotion.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页信息
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo implements Serializable {

    /**
     * 页大小
     */
    private Integer size;

    /**
     * 第几页，从0开始
     */
    private Integer page;

    /**
     * 如果无效获取默认值
     *
     * @return
     */
    public PageInfo getDefaultValueIfInvalid() {
        if (null == size || null == page || size <= 0 || page < 0)
            return new PageInfo(10, 0);
        else
            return this;
    }
}
