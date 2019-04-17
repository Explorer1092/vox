package com.voxlearning.utopia.service.newhomework.api.mapper.termreport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zhangbin
 * @since 2016/11/1 15:30
 */
@Getter
@Setter
@ToString
public class StudentUnitReportBO implements Serializable {

    private static final long serialVersionUID = 4770387222607199483L;
    private String studentId; //学生ID
    private String studentName; //学生名称
    private Integer onTimeNum; //按时完成次数
    private Integer makeupNum; //补做次数
    private Integer notDoneNum; //未做次数
    private Integer avgScore; //平均分
    private String doHomeworkDuration; //总作业时长(时间格式：x小时x分)
    private Double attendanceRate; //出勤率
}
