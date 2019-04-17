package com.voxlearning.utopia.service.ai.data;

/**
 * @author guangqing
 * @since 2019/2/22
 */
public enum UGCWeekPointsEnum {

    CS("听说，交流能力", 1),
    P("发音准确", 2),
    L("词汇量", 3),
    G("语法", 4);
    private String desc;
    private int level;//优先级

    public String getDesc() {
        return desc;
    }

    public int getLevel() {
        return level;
    }

    UGCWeekPointsEnum(String desc, int level) {
        this.desc = desc;
        this.level = level;
    }
}
