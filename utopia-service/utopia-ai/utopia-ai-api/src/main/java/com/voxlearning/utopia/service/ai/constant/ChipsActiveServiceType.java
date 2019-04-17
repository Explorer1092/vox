package com.voxlearning.utopia.service.ai.constant;

/**
 * 主动服务类型
 * @author zhuxuan
 */
public enum ChipsActiveServiceType {
    SERVICE("主动服务"),
    REMIND("未完课提醒"),
    BINDING("绑定公众号"),
    USEINSTRUCTION("薯条英语开课指导"),
    RENEWREMIND("续费提醒"),
    UNKNOW("未定义")
    ;
    private String desc;

    ChipsActiveServiceType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static ChipsActiveServiceType of(String str) {
        try {
            return ChipsActiveServiceType.valueOf(str);
        } catch (Exception e) {
            return UNKNOW;
        }
    }
}
