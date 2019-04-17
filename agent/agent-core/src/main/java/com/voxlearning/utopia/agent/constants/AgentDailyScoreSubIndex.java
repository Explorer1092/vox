package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

/**
 * 日报得分子指标枚举类
 * @author deliang.che
 * @since  2019/3/28
 */
@Getter
public enum AgentDailyScoreSubIndex {
    PLAN_1(1, "填写明日计划"),
    PLAN_2(2, "完成昨日计划"),
    PLAN_3(3, "昨日填写计划未完成"),

    VISIT_TEA_1(4, "省市区组会-参会老师"),
    VISIT_TEA_2(5, "拜访上层资源"),
    VISIT_TEA_3(6, "进校拜访老师"),

    INTO_SCHOOL_QUALITY_1(7, "拜访产生新注册老师"),
    INTO_SCHOOL_QUALITY_2(8, "拜访使得1名未认证或7天以上未使用的认证老师布置作业"),
    INTO_SCHOOL_QUALITY_3(9, "拜访产生1名老师新转化成家长用户"),
    INTO_SCHOOL_QUALITY_4(10, "拜访学校产生5名家长"),
    INTO_SCHOOL_QUALITY_5(11, "拜访“单科低渗”学校"),
    INTO_SCHOOL_QUALITY_6(12, "拜访“频繁进校”标签的学校且序号（8、9、10）分值均为0、频繁拜访需剔除掉“直播展位推广”类型的进校"),
    INTO_SCHOOL_QUALITY_7(13, "产生直播订单（家长工具中订单类的活动的\"今日订单量\"）");

    private Integer code;
    private String desc;

    AgentDailyScoreSubIndex(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public static AgentDailyScoreSubIndex nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

}
