package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

/**
 * 用于区分同一作业下不同标签的作业。
 * 此标签仅用于属于同一个作业卡片下，拥有不同含义的作业。
 * 例如，普通作业有期末复习，类题作业也会有期末复习。
 *
 * @author xuesong.zhang
 * @since 2016/8/9
 */
public enum HomeworkTag {
    Normal("UFO"),
    Last_TermReview("期末复习"), // 上半学期期末复习
    Next_TermReview("期末复习"), // 下半学期期末复习

    // 下面四个目前是自学的
    Correct("错题订正"),
    MentalIncrease("口算练习"),
    WordsIncrease("词汇巩固"),
    SmallPayment("黄金任务"),   // 这个name就忽略吧，写次了
    Platinum("白金任务"),

    KidsDay("儿童节作业"),

    Goal("Goal体系");

    @Getter private final String description;

    HomeworkTag(String description) {
        this.description = description;
    }

    public static HomeworkTag of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return Normal;
        }
    }
}
