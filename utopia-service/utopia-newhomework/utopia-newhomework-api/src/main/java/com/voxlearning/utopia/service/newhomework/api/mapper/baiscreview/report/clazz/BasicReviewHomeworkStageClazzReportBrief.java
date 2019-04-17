package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class BasicReviewHomeworkStageClazzReportBrief implements Serializable {
    private static final long serialVersionUID = 302916151092036162L;
    private Integer stageId;        // 关卡id
    private String stageName;       // 关卡名
    private String homeworkId;      // 关联的作业id
    private int finishUserNum;      // 关卡完成的学生人数
    private String detailUrl;       // 详情地址
    private boolean begin;          // 关卡是否有人复习

    public BasicReviewHomeworkStageClazzReportBrief(Integer stageId, String homeworkId, String stageName) {
        this.stageId = stageId;
        this.homeworkId = homeworkId;
        this.stageName = stageName;
    }
}
