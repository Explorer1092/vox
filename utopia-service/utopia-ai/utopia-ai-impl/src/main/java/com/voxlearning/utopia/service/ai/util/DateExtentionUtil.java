package com.voxlearning.utopia.service.ai.util;

import com.voxlearning.alps.calendar.DateUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

public final class DateExtentionUtil {
    private DateExtentionUtil(){}

    private final static Calendar INSTANCE_DATE = Calendar.getInstance();

    public static int daysDiffExcludeSunnyDay(Date beginDate, Date endDate) {
        int days = 0;
        for (int i = 0; ; i++) {
            Date date = DateUtils.addDays(beginDate, i);
            if (date.after(endDate)) {
                break;
            }
            if (!isSunnyDay(date)) {
                days++;
            }
        }
        return days;
    }
    public static int daysDiffExcludeWeekend(Date beginDate, Date endDate) {
        int days = 0;
        for (int i = 0; ; i++) {
            Date date = DateUtils.addDays(beginDate, i);
            if (date.after(endDate)) {
                break;
            }
            if (!isWeekend(date)) {
                days++;
            }
        }
        return days;
    }

    public static boolean isWeekend(Date date) {
        INSTANCE_DATE.setTime(date);
        return INSTANCE_DATE.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || INSTANCE_DATE.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    public static boolean isSunnyDay(Date date) {
        INSTANCE_DATE.setTime(date);
        return INSTANCE_DATE.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    /**
     * 向上取整算天数
     * @param begin
     * @param end
     * @return
     */
    public static int dayDiffCeil(Date begin, Date end) {
        return new Double(Math.ceil(new BigDecimal(end.getTime() - begin.getTime()).divide(new BigDecimal(1000 * 60 * 60 * 24), 2, BigDecimal.ROUND_HALF_UP).doubleValue())).intValue();
    }
}
