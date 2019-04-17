package com.voxlearning.utopia.service.psr.entity.termreport;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.util.List;


/**
 * Created by mingming.zhao on 2016/10/20.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class TermReportPackage implements Serializable {
    private static final long serialVersionUID = -8470152582567875002L;
    private List<StudentTermReport>StudentTermReports; //所有学生的学期报告
    private List<MonthLayoutInfo>monthLayoutInfos;  // 学期老师每个月布置作业情况
}