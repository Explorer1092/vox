package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 作业类型，目前数学有类题作业。
 * 作业类型出现规则，每出现一种新的作业卡片，理论上就会出现一个新的枚举。
 * <p>
 * 注（坑）：
 * 1为平台的作业（符合平台的规则）
 * 2为自学作业，作业是根据以往历史和错题生成的，非老师布置。
 * 3为第三方作业（特殊的，目前此类作业没有行政班属性，班组关系和平台不通用）
 *
 * @author xuesong.zhang
 * @since 2016-07-07
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum NewHomeworkType {

    Normal(1, "普通作业"),
    Similar(1, "类题作业"),
    WinterVacation(1, "寒假作业"),
    SummerVacation(1, "暑假作业"),
    TermReview(1, "期末复习作业"),
    @Deprecated
    USTalk(3, "一对一作业"),//不再使用，统一YiQiXue
    @Deprecated
    OlympicMath(3, "International Mathematical Olympiad"),//不再使用，统一YiQiXue
    YiQiXue(3, "17xue 作业"),
    selfstudy(2, "自学作业"),
    @Deprecated
    Expand(1, "课外拓展作业"),//不再使用，下线
    BasicReview(1, "期末基础复习"),
    Independent(2, "自主练习"),
    MothersDay(1, "母亲节感恩作业"),
    Activity(1, "节假日活动作业"),
    OutsideReading(1, "课外阅读"),
    OCR(1, "纸质作业"),
    Unknown(0, "UFO");


    @Getter private final Integer typeId; // 见上面注释
    @Getter private final String description;

    public static NewHomeworkType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return Unknown;
        }
    }

    public static final int PlatformType = 1;
    public static final int SelfStudyType = 2;
    public static final int ThirdPartyType = 3;

}
