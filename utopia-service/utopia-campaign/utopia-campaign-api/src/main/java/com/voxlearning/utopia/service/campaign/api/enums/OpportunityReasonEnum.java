package com.voxlearning.utopia.service.campaign.api.enums;

import lombok.Getter;

@Getter
public enum OpportunityReasonEnum {

    布置假期作业("布置假期作业", 2),
    提醒学生("提醒学生", 2),
    分享班级进度("分享班级进度", 1),
    唤醒老师("唤醒老师", 4),
    @Deprecated
    邀请未登录老师("邀请未登录老师", 4),
    邀请老师("邀请老师", 7),
    签到("签到", 1),
    ;

    OpportunityReasonEnum(String name, Integer count) {
        this.name = name;
        this.count = count;
    }

    private String name;
    private Integer count;

    public static OpportunityReasonEnum safeValueOf(String reason) {
        for (OpportunityReasonEnum reasonEnum : OpportunityReasonEnum.values()) {
            if (reasonEnum.name().equals(reason)) {
                return reasonEnum;
            }
        }
        return null;
    }
}
