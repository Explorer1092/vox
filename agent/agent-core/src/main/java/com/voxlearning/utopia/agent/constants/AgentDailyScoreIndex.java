package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

/**
 * 日报得分指标枚举类
 * @author deliang.che
 * @since  2018/11/2
 */
@Getter
public enum AgentDailyScoreIndex {

    WORKLOAD(1, "工作量"),
    VISIT_TEA(2, "见师量"),
    VISIT_MATH_TEA_SCALE(3, "拜访数学老师占比"),
    IF_PLAN(4, "是否有计划"),
    PLAN_FINISH_SCALE(5, "计划完成率"),
    VISIT_TEA_USE_SCALE(6, "拜访老师使用率"),
    VISIT_SCHOOL_NUM(7, "学校拜访频次"),
    TOTAL_SCORE(8, "总得分"),

    PLAN(9, "计划性"),
    INTO_SCHOOL_QUALITY(10, "进校-质量"),
    DAILY_IN_TIME(11, "日报填写及时性");

    private Integer code;
    private String desc;

    AgentDailyScoreIndex(Integer code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public static AgentDailyScoreIndex nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

}
