package com.voxlearning.utopia.agent.mockexam.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 分页结果
 *
 * @author xiaolei.li
 * @version 2018/8/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T extends Serializable> extends Result<ArrayList<T>> implements Serializable {

    /**
     * 页大小
     */
    private Integer size;

    /**
     * 第几页，从0开始
     */
    private Integer page;

    /**
     * 总记录数
     */
    private long totalSize;

    /**
     * 构建一个成功的结果
     *
     * @param data     数据
     * @param pageInfo 分页信息
     * @param count    总记录数
     * @param <T>      数据类型
     * @return 结果
     */
    public static <T extends Serializable> PageResult<T> success(ArrayList<T> data, PageInfo pageInfo, long count) {
        PageResult<T> result = new PageResult<>();
        result.setSuccess(true);
        result.setData(data);
        if (null != pageInfo) {
            result.setSize(pageInfo.getSize());
            result.setPage(pageInfo.getPage());
            result.setTotalSize(count);
        }
        return result;
    }

}
