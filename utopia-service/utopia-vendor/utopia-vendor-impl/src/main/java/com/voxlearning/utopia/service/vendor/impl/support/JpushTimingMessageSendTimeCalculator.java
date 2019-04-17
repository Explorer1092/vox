package com.voxlearning.utopia.service.vendor.impl.support;

/**
 * @author xinxin
 * @since 28/7/2016
 */
public class JpushTimingMessageSendTimeCalculator {
    private static final Long SEND_TIME_DISCRETE_BASED_SECONDS = 5 * 60L;

    public static Long sendTimeCeil(Long originEpochMilli) {
        if (Long.MAX_VALUE < originEpochMilli || originEpochMilli <= 0) {
            return 0L;
        }

        Long originepochSecond = originEpochMilli / 1000;

        if (originepochSecond % SEND_TIME_DISCRETE_BASED_SECONDS == 0) {
            return originepochSecond;
        }

        return originepochSecond - originepochSecond % SEND_TIME_DISCRETE_BASED_SECONDS + SEND_TIME_DISCRETE_BASED_SECONDS;
    }

    public static Long sendTimeFloor(Long originEpochMilli) {
        if (Long.MAX_VALUE < originEpochMilli || originEpochMilli <= 0) {
            return 0L;
        }

        Long originepochSecond = originEpochMilli / 1000;

        if (originepochSecond / SEND_TIME_DISCRETE_BASED_SECONDS == 0) {
            return originepochSecond;
        }

        return originepochSecond - originepochSecond % SEND_TIME_DISCRETE_BASED_SECONDS;
    }
}
