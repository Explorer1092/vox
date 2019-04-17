package com.voxlearning.utopia.service.parent.homework.api.entity;

import java.io.Serializable;

/**
 * 周期单元
 *
 * @author Wenlong Meng
 * @since Feb 25, 2019
 */
public enum PeriodUnit implements Serializable {
    NO(-1, "整活动"),
    DAY(1, "日活动"),
    WEEK(7, "周活动"),
    ;
    public int interval;
    public String desc;

    PeriodUnit(int interval, String desc) {
        this.interval = interval;
        this.desc = desc;
    }

    /**
     * 根据名称获取周期单元，默认为{@link #NO}
     *
     * @param name
     * @return
     */
    public static PeriodUnit of(String name){
        try {
            return valueOf(name);
        }catch (Exception e){

        }
        return NO;
    }
}
