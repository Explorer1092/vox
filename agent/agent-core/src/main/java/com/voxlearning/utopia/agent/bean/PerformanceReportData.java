package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 学校业绩 数据
 * Created by yaguang.wang on 2016/9/20.
 */
@Getter
@Setter
@NoArgsConstructor
public class PerformanceReportData {
    private Long schoolId;              // 学校ID
    private String schoolName;          // 学校名称
    private Long thisMonthCount;        // 本月日浮,本月认证数,本月注册数
    private Long yesterdayCount;        // 昨日日浮,昨日认证数,昨日注册数
    //private Long thisMonthRegAndAuthNum;
    //private Long thisYesterdayRegAndAuthNum;
    private Boolean visited;            // 被访问过的
    private Boolean calPerformance; // 是否参与结算

    public void setSchoolInfo(Long schoolId, String schoolName, Boolean visited) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.visited = visited;
    }
}
