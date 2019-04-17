package com.voxlearning.utopia.service.newhomework.api.util;

import com.voxlearning.alps.calendar.DateUtils;

import java.util.Calendar;
import java.util.Date;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

/**
 * @author guoqiang.li
 * @since 2017/4/19
 */
public class HomeworkTaskUtils {

    /**
     * 计算日常作业任务周期（自然周，周一到周日为一个周期，每周一凌晨更新成初始状态）
     * 返回的是周一的日期(yyyy-MM-dd)
     */
    public static String calculateDailyTaskPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();
        return DateUtils.dateToString(startDate, FORMAT_SQL_DATE);
    }

    /**
     * 计算周末作业任务周期(周五到下周四为一个周期，每周五凌晨更新成初始状态)
     * 返回的是周五的日期(yyyy-MM-dd)
     */
    public static String calculateWeekendTaskPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.FRIDAY);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();
        return DateUtils.dateToString(startDate, FORMAT_SQL_DATE);
    }

    /**
     * 计算运营活动作业任务周期
     * 返回（开始日期yyyyMMdd-结束日期yyyyMMdd）
     */
    public static String calculateActivityTaskPeriod(Date startDate, Date endDate) {
        String startTimeStr = DateUtils.dateToString(startDate, "yyyyMMdd");
        String endTimeStr = DateUtils.dateToString(endDate, "yyyyMMdd");
        return startTimeStr + "-" + endTimeStr;
    }

    /**
     * 计算当前日期是周几(周一为0，周日为6)
     */
    public static int calculateDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        return (currentDay + 5) % 7;
    }

    /**
     * 计算日期date是周几(周一为0，周日为6)
     */
    public static int calculateDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        return (currentDay + 5) % 7;
    }
}
