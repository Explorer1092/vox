package com.voxlearning.wechat.support.utils;

import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qianlong.yang
 * @version 0.1
 * @since 2016/5/6
 */
public class ChinseDateParser {
    // 格式化日期成中文
    private static String formatDigit(String sign) {
        switch (sign) {
            case "0":
                return "零";
            case "1":
                return "一";
            case "2":
                return "二";
            case "3":
                return "三";
            case "4":
                return "四";
            case "5":
                return "五";
            case "6":
                return "六";
            case "7":
                return "七";
            case "8":
                return "八";
            case "9":
                return "九";
            case "10":
                return "十";
            case "11":
                return "十一";
            case "12":
                return "十二";
            default:
                return sign;
        }
    }

    public static Map<String, String> parseDateToChinese(Date date) {
        MonthRange monthRange = new MonthRange(date.getTime(), date.getTime());
        String yearStr = SafeConverter.toString(monthRange.getYear());
        String monthStr = SafeConverter.toString(monthRange.getMonth());
        char[] yearArray = yearStr.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (char c : yearArray) {
            sb.append(formatDigit("" + c));
        }
        yearStr = sb.toString();
        monthStr = formatDigit(monthStr);
        Map<String, String> tuple = new HashMap<>();
        tuple.put("year", yearStr);
        tuple.put("month", monthStr);
        return tuple;
    }
}
