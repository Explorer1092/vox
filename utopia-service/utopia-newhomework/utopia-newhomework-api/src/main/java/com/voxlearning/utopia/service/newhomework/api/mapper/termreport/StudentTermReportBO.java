package com.voxlearning.utopia.service.newhomework.api.mapper.termreport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbin
 * @since 2016/11/7 12:16
 */
@Getter
@Setter
@ToString
public class StudentTermReportBO implements Serializable {
    private static final long serialVersionUID = 2788940498227157656L;
    /**
     * 学生id
     */
    private String studentId;
    /**
     * 学生姓名
     */
    private String studentName;
    /**
     * 每个月完成情况
     */
    private List<MonthDoHomeworkBO> monthDoHomeworkBOList;
    /**
     * 出勤率
     */
    private Double attendanceRate;
    /**
     * 平均分
     */
    private Integer avgScore;
    /**
     * 累计完成次数
     */
    private Integer attendTimes;
}
