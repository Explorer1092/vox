package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-6-8
 */
@Data
public class DisplayVacationHomeworkMapper implements Serializable {
    private static final long serialVersionUID = 3927534253638084253L;

    private Long classId;                               // 班级id
    private String className;                           // 班级名称
    private int studentCount;                          // 班级学生数量
    private String homeworkId;                         // 假期作业id
    private String startDate;                          // 开始时间
    private String endDate;                            // 结束时间
    private String state = "ARRANGE";                 // ("ARRANGE":布置作业,"CANNOT_CHECK":检查作业-灰,"CAN_CHECK_FINISH/CAN_CHECK_OVERDUE":检查作业-亮)
    private boolean assignable = true;               // 是否可以布置假期作业
    private String message;                            // 为什么不能布置假期作业
    private int finishNum;                             // 完成人数
    private int unFinishNum;                           // 为完成人数
    private String bookName;                           // 课本名称
    private int packageCount;                          // 假期作业练习包数量
    private int totalPracticeCount;                    // 假期作业练习总量
    private boolean canCheckReport = false;           // 是否可以查看假期作业历史报告
}
