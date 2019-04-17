package com.voxlearning.utopia.business.api.constant;


/**
 * 走遍美国学英语开学拉新活动类型
 *
 * @author peng.zhang.a
 * @since 16-8-22
 */
public enum LearningGoalType {
    WORDS_100(100), WORDS_300(300), WORDS_500(500), WORDS_600(600),;

    public Integer num;

    LearningGoalType(int num) {
        this.num = num;
    }

    public static LearningGoalType of(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            return null;
        }
    }
}
