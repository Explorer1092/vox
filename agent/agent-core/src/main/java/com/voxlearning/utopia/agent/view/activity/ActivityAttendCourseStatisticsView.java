package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

@Data
public class ActivityAttendCourseStatisticsView {

    private Long id;
    private Integer idType;
    private String name;

    private Integer dayFirstAttendStuCount;       // 指定日期首次上该活动课的学生数
    private Integer totalAttendStuCount;          // 累计上该活动课的学生数

    private Integer dayMeetConditionStuCount;     // 指定日期上课并且满足市场指定条件的学生数
    private Integer totalMeetConditionStuCount;   // 累计上课并且满足市场指定条件的学生数
}
