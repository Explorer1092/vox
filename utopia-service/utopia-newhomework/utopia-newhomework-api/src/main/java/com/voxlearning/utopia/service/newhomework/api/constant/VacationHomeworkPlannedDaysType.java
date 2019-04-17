package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 假期作业计划天数
 * @author: Mr_VanGogh
 * @date: 2018/5/28 下午2:24
 */
@AllArgsConstructor
public enum VacationHomeworkPlannedDaysType {

    // type:1 暑假；2 寒假；3 不分寒暑假

    FortyDays(40, "40天作业包", 1),
    ThirtyFiveDays(35, "35天作业包", 1),
    ThirtyDays(30, "30天作业包", 3),
    TwentyDays(20, "20天作业包", 2),
    TwentyFiveDays(25, "25天作业包", 2);

    @Getter private final Integer days;
    @Getter private final String text;
    @Getter private final Integer type;

    public static VacationHomeworkPlannedDaysType of(String text) {
        try {
            return valueOf(text);
        } catch (Exception ex) {
            return null;
        }
    }

    public static VacationHomeworkPlannedDaysType of(Integer days) {
        for (VacationHomeworkPlannedDaysType t : values()) {
            if (days.equals(t.getDays())) {
                return t;
            }
        }
        return null;
    }

}
