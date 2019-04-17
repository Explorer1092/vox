package com.voxlearning.utopia.service.ai.constant;

/**
 * @author guangqing
 * @since 2019/3/21
 */
public enum AiUserVideoLevel {
    A(">=85，单元>=85", 1),//与没有视频的L对应
    B("<85且A+,单元>=85", 2),//与没有视频的M对应
    C("<85且A+,单元<85", 3),
    D("<85且非A+,单元>=85", 4),
    E("<85且非A+,单元<85", 5),
    //E>D>C>B>A;
    F("所有题目>=85，单元>=85", 7),
    G("所有题目>=85，单元<85", 8),
    H("<85且A+，单元>=85", 9),
    I("<85且A+，单元<85", 10),
    J("<85且非A+，单元>=85", 11),
    K("<85且非A+，单元<85", 12),
    N("所有题目得分都为F", 6),
    //K>J>I>H>G>F>N；

    L("", 1),
    M("", 1),
    UNKNOW("未定义", -1);
    private String desc;
    private int level;//优先级

    public String getDesc() {
        return desc;
    }

    public int getLevel() {
        return level;
    }

    AiUserVideoLevel(String desc, int level) {
        this.desc = desc;
        this.level = level;
    }
}
