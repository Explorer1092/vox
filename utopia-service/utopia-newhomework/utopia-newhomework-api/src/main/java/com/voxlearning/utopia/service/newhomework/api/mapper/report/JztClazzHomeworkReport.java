package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class JztClazzHomeworkReport extends JztHomeworkReport {
    private static final long serialVersionUID = 3214012598456026675L;

    private Integer maxScore; //最高分
    private String maxScoreLevel; //最高等级
    private Integer avgScore; //班级平均分
    private String avgScoreLebel;//班级平均等级
    private Integer finishCount;//完成人数
    private Integer userCount; //学生人数
    private ReportStatus reportStatus; //班级情况报告状态
    private NewHomeworkShareReport shareReport;
    private List<String> shareList; //分享模块列表

    /**
     * 家长通作业班级情况->作业状态枚举
     */
    public enum ReportStatus {
        shared,          // 已分享
        unshared,        // 未分享
        unterminated    // 未过期
    }


}
