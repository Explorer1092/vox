package com.voxlearning.washington.mapper;

import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/3/3
 * Time: 15:26
 * 分页对象
 */


public class Pageable<T, R> implements Serializable {
    private static final long serialVersionUID = 6931102066798840020L;

    @Getter private boolean hasMore = false;   // 展示更多
    private int offset;                        // 数据索引起
    private int limit;                         // 数据size
    @Getter private int total;                 // 数据总数
    private List<T> data;                      // 数据信息
    @Getter @Setter private List<R> records;   // 返回的结果数据
    @Getter private int page;
    @Getter private int pageSize;


    public static <T, R> Pageable<T, R> newBuilder() {
        return new Pageable<>();
    }

    @SuppressWarnings("unchecked")
    public Pageable<T, R> build(int page, int pageSize, List<T> data) {
        this.offset = page > 0 ? (page - 1) * pageSize : 0;
        this.limit = pageSize;
        this.total = CollectionUtils.isEmpty(data) ? 0 : data.size();
        this.data = data;

        this.page = page;
        this.pageSize = pageSize;
        return this;
    }


    public Pageable<T, R> build(int page, int pageSize, int total, List<R> data) {
        this.offset = page > 0 ? (page - 1) * pageSize : 0;
        this.limit = pageSize;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.records = data;
        return this;
    }

    /**
     * 分页处理
     */
    public Pageable<T, R> process(Function<T, R> function) {
        if (this.total <= this.offset) {
            this.records = Collections.emptyList();
            return this;
        } else if (this.total > this.offset && this.total <= (this.offset + this.limit)) {
            //部分数据  hasMore = false;
            processData(function);
            return this;
        } else {
            this.hasMore = true;
            processData(function);
            return this;
        }
    }

    /**
     * 分页处理
     */
    public Pageable<T, R> processArray(Function<List<T>, List<R>> function) {
        if (this.total > (this.offset + this.limit)) {
            this.hasMore = true;
        }
        this.records = function.apply(data);
        return this;
    }

    /**
     * 数据处理
     */
    private void processData(Function<T, R> function) {
        this.records = new LinkedList<>();
        for (int i = this.offset; i < this.total; i++) {
            R r = function.apply(this.data.get(i));
            if (r == null) {
                continue;
            }
            this.records.add(r);
        }
    }

}
