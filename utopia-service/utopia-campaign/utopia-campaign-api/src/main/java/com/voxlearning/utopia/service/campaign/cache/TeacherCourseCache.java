package com.voxlearning.utopia.service.campaign.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareConstants.CACHE_END_DATE;

public class TeacherCourseCache {

    public static UtopiaCache getCache() {
        return TeacherCourseCacheHolder.teacherCourseCache;
    }

    public static UtopiaCache getPersistCache() {
        return TeacherCourseCacheHolder.teacherCoursePersistCache;
    }

    private static Date RANKING_CACHE_END_DATE = DateUtils.stringToDate("2019-10-01 00:00:00", DateUtils.FORMAT_SQL_DATETIME);

    private static class TeacherCourseCacheHolder {
        private static final UtopiaCache teacherCourseCache;

        private static final UtopiaCache teacherCoursePersistCache;

        static {
            teacherCourseCache = CacheSystem.CBS.getCache("flushable");
            teacherCoursePersistCache = CacheSystem.CBS.getCache("storage");
        }
    }

    public static List<Map<String, Object>> loadExcellentTotalRankData(String subject, String date) {
        if (date == null) date = CACHE_END_DATE;
        String ck = ckExcellentTotalRank(subject, date);
        return getPersistCache().load(ck);

    }

    public static void setExcellentTotalRankData(String subject, String date, List<Map<String, Object>> rankingData) {
        String ck = ckExcellentTotalRank(subject, date);
        getPersistCache().set(ck, (int) (RANKING_CACHE_END_DATE.getTime() / 1000), rankingData);
    }

    private static String ckExcellentTotalRank(String subject, String date) {
        return "TS_ExcellentRank_T_" + subject + "_" + date;
    }

    public static List<Map<String, Object>> loadCanvassTopData(String subject, String date) {
        String ck = ckCanvassTopData(subject, date);
        return getPersistCache().load(ck);
    }

    public static void setExcellentCanvassTopData(String subject, String date, List<Map<String, Object>> rankingData) {
        String ck = ckCanvassTopData(subject, date);
        getPersistCache().set(ck, (int) (RANKING_CACHE_END_DATE.getTime() / 1000), rankingData);
    }

    private static String ckCanvassTopData(String subject, String date) {
        return "TS_ExcellentCanvassTop_T_" + subject + "_" + date;
    }

    public static List<Map<String, Object>> loadExcellentWeeklyRankData(String subject, Integer week) {
        String ck = ckExcellentWeeklyRank(subject, week);
        return getPersistCache().load(ck);
    }

    public static void setExcellentWeeklyRankData(String subject, Integer week, List<Map<String, Object>> rankingData) {
        String ck = ckExcellentWeeklyRank(subject, week);
        getPersistCache().set(ck, (int) (RANKING_CACHE_END_DATE.getTime() / 1000), rankingData);
    }

    private static String ckExcellentWeeklyRank(String subject, Integer week) {
        return "TS_ExcellentRank_W_" + subject + "_" + week;
    }

    public static List<Map<String, Object>> loadExcellentMonthlyRankData(String subject, Integer month) {
        String ck = ckExcellentMonthlyRank(subject, month);
        return getPersistCache().load(ck);
    }

    public static void setExcellentMonthlyRankData(String subject, Integer month, List<Map<String, Object>> rankingData) {
        String ck = ckExcellentMonthlyRank(subject, month);
        getPersistCache().set(ck, (int) (RANKING_CACHE_END_DATE.getTime() / 1000), rankingData);
    }

    private static String ckExcellentMonthlyRank(String subject, Integer month) {
        return "TS_ExcellentRank_M_" + subject + "_" + month;
    }

    public static Integer loadCourseShareNum(String courseId) {
        String ck = ckCourseShareNum(courseId);
        return getCache().load(ck);
    }

    public static void setCourseShareNum(String courseId, Integer shareNum) {
        String ck = ckCourseShareNum(courseId);
        getCache().set(ck, DateUtils.getCurrentToDayEndSecond(), shareNum);
    }

    public static void clearCourseShareNumCache(String courseId) {
        String ck = ckCourseShareNum(courseId);
        getCache().delete(ck);
    }

    private static String ckCourseShareNum(String courseId) {
        return "TS_COURSE_SHARENUM_" + courseId;
    }

}
