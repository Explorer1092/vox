package com.voxlearning.utopia.service.action.api.support;

import lombok.Getter;

/**
 * @author xinxin
 * @since 12/8/2016
 */
public enum AchievementType {
    HuanXingShi("唤醒师", true),
    ZiXueChengCai("自学成才", true),
    @Deprecated XingGuangCuiCan("星光璀璨", false),
    @Deprecated ShiQueBuYi("查漏补缺", false),
    @Deprecated YongWangZhiQian("勇往直前", false),
    QinXueKuLian("勤学苦练", true),
    ShenSuanZi("神算子", true),
    JinHuaTong("金话筒", true),
    YueDuDaKa("阅读大咖", true),
    XueYouSuoCheng("学有所成", true),
    @Deprecated YouCuoBiJiu("有错必纠", false);

    @Getter private String title;
    @Getter private Boolean valid;


    AchievementType(String title, Boolean valid) {
        this.title = title;
        this.valid = valid;
    }

    public static AchievementType ofTitle(String title) {
        for (AchievementType t : values()) {
            if (title.equals(t.getTitle())) {
                return t;
            }
        }

        return null;
    }

    public static AchievementType of(String name) {
        for (AchievementType t : values()) {
            if (name.equals(t.name())) {
                return t;
            }
        }

        return null;
    }
}
