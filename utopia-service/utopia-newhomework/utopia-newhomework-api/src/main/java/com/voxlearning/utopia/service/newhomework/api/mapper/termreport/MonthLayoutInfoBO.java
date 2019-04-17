package com.voxlearning.utopia.service.newhomework.api.mapper.termreport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zhangbin
 * @since 2016/11/7 13:18
 */
@Getter
@Setter
@ToString
public class MonthLayoutInfoBO implements Serializable {

    private static final long serialVersionUID = 2477194223415342343L;
    /**
     * 月份
     */
    private String month;
    /**
     * 老师布置了多少次
     */
    private Integer layoutCount;
}
