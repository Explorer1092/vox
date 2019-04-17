package com.voxlearning.utopia.service.zone.api.constant;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/2/28
 * Time: 15:55
 */
public enum ClazzRecordTypeEnum {

    UNKNOWN(100, "未知记录", false),
    CHINESE_STAR(101, "语文之星", true),
    MATH_STAR(102, "数学之星", true),
    ENGLISH_STAR(103, "英语之星", true),
    FOCUS_STAR(104, "专注之星", true),
    FULLMARKS_STAR(104, "满分之星", false),
    FRIENDSHIP_STAR(105, "友爱之星", false),
    SHARP_STAR(106, "明察之星", false),
    FASHION_STAR(107, "装扮之星", false),
    STAMINA_STAR(108, "毅力之星", false),
    CRACKER_STAR(109, "闯关之星", false),
    COMPETE_STAR(110, "竞技之星", false),
    ;

    ClazzRecordTypeEnum(int id, String desc, boolean isHomeworkRecord) {
        this.id = id;
        this.desc = desc;
        this.isHomeworkRecord = isHomeworkRecord;
    }

    @Getter private final int id;
    @Getter private final String desc;
    @Getter private final boolean isHomeworkRecord; //是否为作业类记录

    private static final Map<String, ClazzRecordTypeEnum> map;

    static {
        map = Arrays.stream(values()).collect(Collectors.toMap(ClazzRecordTypeEnum::name, Function.identity()));
    }

    public static ClazzRecordTypeEnum safeParse(String name) {
        return map.getOrDefault(name, UNKNOWN);
    }

    /**
     * @param subject 学科
     * @return 语文-语文之星；数学-数学之星；英语-英语之星；其他-null
     */
    public static ClazzRecordTypeEnum getFromSubject(Subject subject) {

        switch (subject) {
            case MATH:
                return ClazzRecordTypeEnum.MATH_STAR;
            case CHINESE:
                return ClazzRecordTypeEnum.CHINESE_STAR;
            case ENGLISH:
                return ClazzRecordTypeEnum.ENGLISH_STAR;
            default:
                return null;
        }

    }
}
