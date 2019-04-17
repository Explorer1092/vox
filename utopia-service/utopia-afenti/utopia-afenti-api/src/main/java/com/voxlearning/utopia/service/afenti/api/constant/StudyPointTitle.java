package com.voxlearning.utopia.service.afenti.api.constant;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;

/**
 * @author peng.zhang.a
 * @since 2016/5/16
 * 做题报告累计称号
 */

@Getter
public enum StudyPointTitle {
    NUM_MIN_0(0, 0, "默默无名", "默默无名", "默默无名", 0),
    NUM_1_5(1, 5, "英语小能手", "数学小能手", "语文小能手", 1),
    NUM_1_10(6, 10, "英语小组长", "数学小组长", "语文小组长", 2),
    NUM_11_20(11, 20, "英语大班长", "数学大班长", "语文大班长", 3),
    NUM_21_50(21, 50, "英语牛人", "数学牛人", "语文牛人", 4),
    NUM_51_100(51, 100, "英语学霸", "数学学霸", "语文学霸", 5),
    NUM_101_200(101, 200, "英语小专家", "数学小专家", "语文小专家", 6),
    NUM_201_500(201, 501, "英语小博士", "数学小博士", "语文小博士", 7),
    NUM_501_MAX(500, Integer.MAX_VALUE, "英语大明星", "数学大明星", "语文大明星", 8),
    NUM_ERROR(Integer.MIN_VALUE, -1, "没有称号", "没有称号", "没有称号", -1),;

    private Integer min;
    private Integer max;
    private String desc;
    private String mathDesc;
    private String chineseDesc;
    private Integer level;

    StudyPointTitle(Integer min, Integer max, String desc, String mathDesc, String chineseDesc, int level) {
        this.min = min;
        this.max = max;
        this.desc = desc;
        this.mathDesc = mathDesc;
        this.chineseDesc = chineseDesc;
        this.level = level;
    }

    public static StudyPointTitle findInRange(int num) {
        for (StudyPointTitle studyPointTitle : StudyPointTitle.values()) {
            if (num >= studyPointTitle.min && num <= studyPointTitle.max) {
                return studyPointTitle;
            }
        }
        return NUM_ERROR;
    }

    public static String findDescInRange(int num, Subject subject) {
        for (StudyPointTitle studyPointTitle : StudyPointTitle.values()) {
            if (num >= studyPointTitle.min && num <= studyPointTitle.max) {
                return studyPointTitle.fetchDesc(subject);
            }
        }
        return NUM_ERROR.desc;
    }

    public static String findDescInRangeByLevel(int level, Subject subject) {
        for (StudyPointTitle studyPointTitle : StudyPointTitle.values()) {
            if (level == studyPointTitle.getLevel()) {
                return studyPointTitle.fetchDesc(subject);
            }
        }
        return NUM_ERROR.desc;
    }

    public String fetchDesc(Subject subject) {
        switch (subject) {
            case ENGLISH:
                return desc;
            case MATH:
                return mathDesc;
            case CHINESE:
                return chineseDesc;
        }
        return NUM_ERROR.desc;
    }
}
