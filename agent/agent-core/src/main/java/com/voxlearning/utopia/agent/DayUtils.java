package com.voxlearning.utopia.agent;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * DayUtils
 *
 * @author song.wang
 * @date 2016/5/25
 */
public class DayUtils {

    private static final Logger logger = LoggerFactory.getLogger(DayUtils.class);

    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyyMMdd");

    public static final List<Integer> holidayList = new ArrayList<>(); // 周一到周五中放假快活的日子
    public static final List<Integer> weekendWorkdayList = new ArrayList<>(); // 周六日苦逼上班的日子

    static {
        holidayList.add(20180101);
        holidayList.add(20180215);
        holidayList.add(20180216);
        holidayList.add(20180219);
        holidayList.add(20180220);
        holidayList.add(20180221);
        holidayList.add(20180405);
        holidayList.add(20180406);
        holidayList.add(20180430);
        holidayList.add(20180501);
        holidayList.add(20180618);
        holidayList.add(20180924);
        holidayList.add(20181001);
        holidayList.add(20181002);
        holidayList.add(20181003);
        holidayList.add(20181004);
        holidayList.add(20181005);
        holidayList.add(20181231);

        holidayList.add(20190101);
        holidayList.add(20190204);
        holidayList.add(20190205);
        holidayList.add(20190206);
        holidayList.add(20190207);
        holidayList.add(20190208);
        holidayList.add(20190405);
        holidayList.add(20190501);
        holidayList.add(20190607);
        holidayList.add(20190913);
        holidayList.add(20191001);
        holidayList.add(20191002);
        holidayList.add(20191003);
        holidayList.add(20191004);
        holidayList.add(20191007);


        weekendWorkdayList.add(20180211);
        weekendWorkdayList.add(20180224);
        weekendWorkdayList.add(20180408);
        weekendWorkdayList.add(20180428);
        weekendWorkdayList.add(20180929);
        weekendWorkdayList.add(20180930);
        weekendWorkdayList.add(20181229);

        weekendWorkdayList.add(20190202);
        weekendWorkdayList.add(20190203);
        weekendWorkdayList.add(20190929);
        weekendWorkdayList.add(20191012);
    }

    // 两个日期之间的日期列表，包括开始日期， 包括结束日期
    public static List<Integer> getEveryDays(Integer startDay, Integer endDay){
        if(endDay < startDay){
            return Collections.emptyList();
        }
        List<Integer> dayList = new ArrayList<>();
        try {
            Date startDate = DATE_FORMAT.parse(String.valueOf(startDay));
            Date endDate = DATE_FORMAT.parse(String.valueOf(endDay));
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            while (!startCal.after(endCal)) {
                dayList.add(ConversionUtils.toInt(DATE_FORMAT.format(startCal)));
                startCal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }catch (Exception e){
        }
        return dayList;
    }

    // 两个日期之间的工作日列表，包括开始日期， 不包括结束日期
    // 去除了周六日，节假日，加上周六日调休的数据
    public static List<Integer> getWorkdayList(Integer startDay, Integer endDay){
        if(endDay < startDay){
            return Collections.emptyList();
        }
        List<Integer> dayList = new ArrayList<>();
        try {
            Date startDate = DATE_FORMAT.parse(String.valueOf(startDay));
            Date endDate = DATE_FORMAT.parse(String.valueOf(endDay));
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            while (startCal.before(endCal)) {
                int day = ConversionUtils.toInt(DATE_FORMAT.format(startCal));
                int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
                if((dayOfWeek > Calendar.SUNDAY && dayOfWeek < Calendar.SATURDAY && !holidayList.contains(day))
                        || ((dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) && weekendWorkdayList.contains(day))){
                    dayList.add(day);
                }
                startCal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }catch (Exception e){
            logger.error("DayUtils.getWorkdayList - startDay : {}; endDay : {}; Exception = {}", startDay, endDay, e);
        }
        return dayList;
    }

    /**
     * 判断是否是工作日
     * @param day
     * @return
     */
    public static Boolean isWorkDay(Integer day){
        if (day == null){
            return false;
        }
        try {
            Date date = DATE_FORMAT.parse(String.valueOf(day));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if((dayOfWeek > Calendar.SUNDAY && dayOfWeek < Calendar.SATURDAY && !holidayList.contains(day))
                    || ((dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) && weekendWorkdayList.contains(day))){
                return true;
            }
        } catch (Exception e) {
            logger.error("DayUtils.isWorkDay - day : {}; Exception = {}", day, e);
        }
        return false;
    }

    public static Date getFirstDayOfMonth(Date date){
        if(date == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String dateStr = DateUtils.dateToString(calendar.getTime(), "yyyyMMdd");
        if(StringUtils.isBlank(dateStr)){
            return null;
        }
        return DateUtils.stringToDate(dateStr, "yyyyMMdd");
    }

    public static Date getLastDayOfMonth(Date date){
        if(date == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String dateStr = DateUtils.dateToString(calendar.getTime(), "yyyyMMdd");
        if(StringUtils.isBlank(dateStr)){
            return null;
        }
        return DateUtils.stringToDate(dateStr, "yyyyMMdd");
    }

    public static Date addDay(Date date, int delta){
        if(date == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, delta);
        return calendar.getTime();
    }

    public static int getMonth(Date date){
        if(date == null){
            return -1;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }

    public static int getDay(Date date){
        if(date == null){
            return -1;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    // 判断指定的时间是否在给定的时间范围内
    public static boolean judgeDateRange(Date startTime, Date endTime, Long time) {
        if (time == null) {
            return false;
        }
        Calendar start = Calendar.getInstance();
        start.setTime(startTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.after(start) && calendar.before(end);
    }

    // 获取指定时间所在的月份有多少天
    public static int getMonthDays(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static Date addMonth(Date date, Integer month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, month);
        return calendar.getTime();
    }

    public static Date addWeek(Date date, Integer week){
        if(date == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.WEEK_OF_YEAR, week);
        return calendar.getTime();
    }

    public static Date getFirstDayOfWeek(Date date){
        if(date == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, 1);
        String dateStr = DateUtils.dateToString(calendar.getTime(), "yyyyMMdd");
        if(StringUtils.isBlank(dateStr)){
            return null;
        }
        return DateUtils.stringToDate(dateStr, "yyyyMMdd");
    }

    public static Date getLastDayOfWeek(Date date){
        if(date == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK));
        String dateStr = DateUtils.dateToString(calendar.getTime(), "yyyyMMdd");
        if(StringUtils.isBlank(dateStr)){
            return null;
        }
        return DateUtils.stringToDate(dateStr, "yyyyMMdd");
    }


    public static Date getMinDate(Collection<Integer> days){
        if(CollectionUtils.isEmpty(days)){
            return new Date();
        }
        Integer targetDay = days.stream().min(Comparator.comparing(Function.identity())).orElse(null);
        if(targetDay == null){
            return new Date();
        }
        return DateUtils.stringToDate(String.valueOf(targetDay), "yyyyMMdd");
    }

    public static Date getMaxDate(Collection<Integer> days){
        if(CollectionUtils.isEmpty(days)){
            return new Date();
        }
        Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).orElse(null);
        if(targetDay == null){
            return new Date();
        }
        return DateUtils.stringToDate(String.valueOf(targetDay), "yyyyMMdd");
    }
}
