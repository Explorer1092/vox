package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 工作记录统计信息
 * @author tao.zang
 * @since 2017/3/1
 */
@Getter
@Setter
public class WorkRecordStatistics implements Serializable {
    private Long workUserId ;       //当前工作记录所属人员id标识
    private String workUserRealName; //当前工作记录所属人员真实姓名
    private int intoSchoolCount;//进校记录总数
    private int groupMeetingCount; // 组会记录总数
    private int visitCount;         //陪访记录总数
    private int researchersCount;   //教研员拜访记录总数
    private double totals;
    private double avgWorkload;

    private Double userIntoSchoolWorkload;                   // 进校工作量
    private Double userVisitWorkload;                        // 陪访工作量
    private Double userMeetingWorkload;                      // 组会&参与组会工作量
    private Double userTeachingWorkload;                     // 拜访教研员工作量
    private Double userWorkload;                             // 全部工作量
}
