package com.voxlearning.utopia.service.newhomework.api.mapper.termreport;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbin
 * @since 2016/11/7 12:04
 */
@Getter
@Setter
@ToString
public class TermReportBO implements Serializable {

    private static final long serialVersionUID = -5496045945322786249L;
    private String dateRange;  //结构yearId_termId，例子：2017_0
    private Long groupId;
    private Subject subject;
    /**
     * 所有学生的学期报告
     */
    private List<StudentTermReportBO> studentTermReportList;
    /**
     * 学期老师每个月布置作业情况
     */
    private List<MonthLayoutInfoBO> monthLayoutInfoList;
    /**
     * 本学期所有月份布置作业次数
     */
    private Integer totalMonthLayoutTimes;
}
