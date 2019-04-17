package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.personal;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class BasicReviewHomeworkStagePersonalReportBrief implements Serializable {
    private static final long serialVersionUID = 3250975684520831732L;
    private Integer stageId;        // 关卡id
    private String stageName;       // 关卡名
    private String homeworkId;      // 关联的作业id
    private int wrongNum;           // 错误数
    private int stageQuestionNum;   // 关卡题目数量
    private boolean begin;          // 是否复习
    private boolean finished;
}
