package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 团队专员的业绩
 * Created by yaguang.wang on 2016/7/26.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamPerformanceData implements Serializable {

    private static final long serialVersionUID = -7307550818131524837L;
    private Long workerId;                   // 团队人员ID
    private String workerName;                 // 团队人员姓名
    // 进校及计划的数据
    private Integer thisMonthIntoSchoolCount;  //本月进校数
    private Integer inPlanCount;               //计划内进校数量
    private Integer thisTodayIntoSchoolCount;  //今日进校数
    private Integer tomorrowPlanCount;         //明日计划数

    // 业绩的数据
    private Double juniorSascCompleteRate; //小学本月单活
    private Double juniorDascCompleteRate; //小学本月双活
    private Double middleSascCompleteRate; //中学本月单活
}
