package com.voxlearning.utopia.service.newhomework.api.mapper.termreport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zhangbin
 * @since 2016/11/7 13:11
 */
@Getter
@Setter
@ToString
public class MonthDoHomeworkBO implements Serializable {
    private static final long serialVersionUID = 7830533218499177215L;
    /**
     * 月份
     */
    private String month;
    /**
     * 当月完成次数
     */
    private Integer completeCount;
}
