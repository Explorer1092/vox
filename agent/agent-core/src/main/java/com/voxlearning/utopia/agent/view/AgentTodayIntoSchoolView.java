package com.voxlearning.utopia.agent.view;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yaguang.wang
 * on 2017/10/9.
 */
@Getter
@Setter
public class AgentTodayIntoSchoolView extends BaseTodayIntoSchoolView {
    private String agentName;
    private Long agentId;
    private String nextUrl;
    private double intoSchoolCountAvg;     // 人均进校数

    //  今日部门维度专员的进校信息
    public AgentTodayIntoSchoolView(String agentName, Long agentId, String nextUrl, Double intoSchoolCountAvg, Double visitTeacherAvg) {
        this.agentName = agentName;
        this.agentId = agentId;
        this.nextUrl = nextUrl;
        this.intoSchoolCountAvg = intoSchoolCountAvg;
        this.visitTeacherAvg = visitTeacherAvg;
    }

    // 今日专员维度的进校信息
    public AgentTodayIntoSchoolView(String agentName, Long agentId, String nextUrl, BaseTodayIntoSchoolView bdView) {
        this.agentName = agentName;
        this.agentId = agentId;
        this.nextUrl = nextUrl;
        if (bdView != null) {
            this.intoSchoolCount = bdView.getIntoSchoolCount();
            this.visitTeacherAvg = bdView.getVisitTeacherAvg();
        }
    }
}
