package com.voxlearning.utopia.agent.view.daily;

import lombok.Data;

@Data
public class DailyScoreView {
    private double totalScore;//总得分
    private double makePlanScore;//制定计划得分
    private double finishPlanScore;//完成计划得分

    private double visitTeaScore;//拜访老师/资源得分
    private double teaRegTeaAppScore;//老师注册教师端得分
    private double unActivityTeaAssignHwScore;//不活跃老师布置作业
    private double teaRegParentAppScore;//老师注册家长通
    private double newParentScore;//带来新家长得分
    private double visitLowPermeateSchoolScore;//拜访低渗校
    private double liveBroadcastOrderScore;//直播订单

    private double dailyInTimeScore;//按时提交日报得分

    private double planScore;//计划性得分
    private double workloadScore;//工作量得分
    private double intoSchoolQualityScore;//进校质量得分


}
