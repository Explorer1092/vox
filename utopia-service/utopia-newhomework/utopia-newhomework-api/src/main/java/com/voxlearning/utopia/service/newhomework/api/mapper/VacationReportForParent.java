package com.voxlearning.utopia.service.newhomework.api.mapper;


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class VacationReportForParent implements Serializable {
    private static final long serialVersionUID = 6030468732512569529L;
    private Subject subject;//学科
    private boolean begin;//是否开始作业
    private boolean finish;//是否完成作业
    private String endTime;//作业截止时间
    private int finishedVacationHomework;//作业學生完成数量
    private int totalHomeworkNum; //整個假期的作业份数
    private VacationHomeworkPackage.Location location;
}
