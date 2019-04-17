package com.voxlearning.utopia.service.campaign.api.constant;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;

import java.util.Date;

public class TeacherCoursewareConstants {

    public static Date RANKING_START_DATE = DateUtils.stringToDate("2018-10-29 00:00:00", DateUtils.FORMAT_SQL_DATETIME);

    public static final String OP_CANVASS = "canvass";

    public static Date CANVASS_VOTE_END_DATE = DateUtils.stringToDate("2018-12-31 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
    // public static Date CANVASS_END_DATE = DateUtils.stringToDate("2019-01-16 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
    public static Date TOTAL_RANK_END = DateUtils.stringToDate("2018-12-22 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
    public static Date WEEKLY_TIMES_END = DateUtils.stringToDate("2018-12-18 00:00:00", DateUtils.FORMAT_SQL_DATETIME);

    public static final String CACHE_END_DATE = "2018-12-16";

    public static Date UPLOAD_END_DATE = DateUtils.stringToDate(RuntimeMode.isProduction() ?
            "2018-12-15 23:59:59" : "2018-12-12 23:59:59", DateUtils.FORMAT_SQL_DATETIME);

    public static MapMessage CLOSE_UPLOAD_MSG = MapMessage.errorMessage("上传作品通道已关闭");

    public static boolean isCloseUpload() {
        return new Date().getTime() > UPLOAD_END_DATE.getTime();
    }

    // 计算总榜有效期
    public static int getCacheEnd() {
        Date now = new Date();
        Date date = DateUtils.addDays(now, 30);
        return (int) ((date.getTime() - now.getTime()) / 1000);
    }

}
