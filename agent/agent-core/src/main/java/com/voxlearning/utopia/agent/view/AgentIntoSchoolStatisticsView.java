package com.voxlearning.utopia.agent.view;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yaguang.wang
 * on 2017/10/9.
 */

@Getter
@Setter
public class AgentIntoSchoolStatisticsView extends BaseIntoSchoolStatisticsView {
    private String agentName;
    private Long agentId;

    private String nextUrl;
    private double intoSchoolCountAvg;     // 人均进校数

    //  本月部门维度专员的进校信息
    public AgentIntoSchoolStatisticsView(String agentName, Long agentId, String nextUrl, Double intoSchoolCountAvg, Double visitTeacherAvg, String visitTeacherHwPro) {
        this.agentName = agentName;
        this.agentId = agentId;

        this.nextUrl = nextUrl;
        this.intoSchoolCountAvg = intoSchoolCountAvg;
        this.visitTeacherAvg = visitTeacherAvg;
        this.visitTeacherHwPro = visitTeacherHwPro;
    }

    // 本月专员维度的进校信息
    public AgentIntoSchoolStatisticsView(String agentName, Long agentId,  String nextUrl, BaseIntoSchoolStatisticsView bdView) {
        this.agentName = agentName;
        this.agentId = agentId;
        this.nextUrl = nextUrl;
        if (bdView != null) {
            this.intoSchoolCount = bdView.getIntoSchoolCount();
            this.visitedSchoolCount = bdView.getVisitedSchoolCount();
            this.schoolTotal = bdView.getSchoolTotal();
            this.visitTeacherAvg = bdView.getVisitTeacherAvg();
            this.visitTeacherHwPro = bdView.getVisitTeacherHwPro();
        }
    }
}
