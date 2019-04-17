package com.voxlearning.utopia.service.mizar.api.utils;


import java.util.Date;

public class DateParseHelper {

    private static final int ONE_MINUTE = 60 * 1000;
    private static final int ONE_HOUR = 60 * ONE_MINUTE;
    private static final int ONE_DAY = 24 * ONE_HOUR;

    /**
     * 根据所秒数,计算相差的时间并以**天**时**分返回
     */
    public static String parseSec2Str(long m) {
        StringBuilder desc = new StringBuilder();
        int nDay = (int) m / ONE_DAY;
        int nHour = (int) (m - nDay * ONE_DAY) / ONE_HOUR;
        int nMinute = (int) (m - nDay * ONE_DAY - nHour * ONE_HOUR) / ONE_MINUTE;
        if (nDay > 0) {
            desc.append(nDay).append("天");
        }
        if (nHour > 0) {
            desc.append(nHour).append("小时");
        }
        if (nMinute > 0) {
            desc.append(nMinute).append("分钟");
        }
        return desc.toString();
    }

    public static String calDuration(Date startTime, Date endTime, String defaultTime) {
        if (startTime == null || endTime == null || endTime.before(startTime)) {
            return defaultTime;
        }
        return parseSec2Str(endTime.getTime() - startTime.getTime());
    }

}
