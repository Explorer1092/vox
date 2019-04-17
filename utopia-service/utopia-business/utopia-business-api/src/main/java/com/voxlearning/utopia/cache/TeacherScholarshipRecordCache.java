package com.voxlearning.utopia.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.entity.activity.TeacherAssignTermReviewRecord;

import java.util.*;

/**
 * 期末复习布置作业缓存使用工具
 * @author liuyong 20181130
 */
public class TeacherScholarshipRecordCache {
    private static Date TERMREVIEW_CACHE_END_DATE = DateUtils.stringToDate("2019-03-01 00:00:00", DateUtils.FORMAT_SQL_DATETIME);

    public static final UtopiaCache teacherScholarshipRecordCache = CacheSystem.CBS.getCache("persistence");

    public static void addRecords(List<TeacherAssignTermReviewRecord> records){
        TeacherAssignTermReviewRecord record = records.get(0);
        teacherScholarshipRecordCache.set(ckTeacherAssignTermReview(record.getTeacherId()), (int) (TERMREVIEW_CACHE_END_DATE.getTime() / 1000),records);
    }

    public static List<TeacherAssignTermReviewRecord> loadRecords(Long teacherId){
        List<TeacherAssignTermReviewRecord> datas = teacherScholarshipRecordCache.load(ckTeacherAssignTermReview(teacherId));
        return datas;
    }

    public static List<TeacherAssignTermReviewRecord> loadRecords(Collection<Long> teacherIds){
        List<TeacherAssignTermReviewRecord> records = new ArrayList<>();
        for(Long teacherId : teacherIds){
            List<TeacherAssignTermReviewRecord> datas = teacherScholarshipRecordCache.load(ckTeacherAssignTermReview(teacherId));
            if(CollectionUtils.isNotEmpty(datas)){
                records.addAll(datas);
            }
        }
        return records;
    }

    public static void clearRecords(Long teacherId) {
        String ck = ckTeacherAssignTermReview(teacherId);
        teacherScholarshipRecordCache.delete(ck);
    }

    public static void setApplyYuandingdouCache(Long teacherId){
        String key = "TeacherAssignTermReview_yuandingdou_apply_"+teacherId;
        Date todayEnd = DateUtils.getTodayEnd();
        teacherScholarshipRecordCache.set(key, (int) (todayEnd.getTime() / 1000),true);
    }

    public static boolean getApplyYuandingdouCache(Long teacherId){
        String key = "TeacherAssignTermReview_yuandingdou_apply_"+teacherId;
        return SafeConverter.toBoolean(teacherScholarshipRecordCache.load(key));
    }

    public static void setApplyDayTermReviewCache(Long teacherId){
        String key = "TeacherAssignTermReview_dayTermReview_apply_"+teacherId;
        Date todayEnd = DateUtils.getTodayEnd();//有效期到当天结束
        teacherScholarshipRecordCache.set(key, (int) (todayEnd.getTime() / 1000),true);
    }

    public static boolean getApplyDayTermReviewCache(Long teacherId){
        String key = "TeacherAssignTermReview_dayTermReview_apply_"+teacherId;
        return SafeConverter.toBoolean(teacherScholarshipRecordCache.load(key));
    }

    public static void main(String[] args) {
        Date weekEnd = DateUtils.getLastDayOfWeek(new Date());
        System.out.println(weekEnd);
        Date todayEnd = DateUtils.getTodayEnd();//有效期到当天结束
        System.out.println(todayEnd);
    }

    public static void setApplyWeekTermReviewCache(Long teacherId){
        String key = "TeacherAssignTermReview_weekTermReview_apply_"+teacherId;
        Date weekEnd = DateUtils.getLastDayOfWeek(new Date());//有效期到本周末结束
        teacherScholarshipRecordCache.set(key, (int) (weekEnd.getTime() / 1000),true);
    }

    public static boolean getApplyWeekTermReviewCache(Long teacherId){
        String key = "TeacherAssignTermReview_weekTermReview_apply_"+teacherId;
        return SafeConverter.toBoolean(teacherScholarshipRecordCache.load(key));
    }

    public static void setApplyMonthTermReviewCache(Long teacherId){
        String key = "TeacherAssignTermReview_MonthTermReview_apply_"+teacherId;
        teacherScholarshipRecordCache.set(key,  (int) (TERMREVIEW_CACHE_END_DATE.getTime() / 1000),true);
    }

    public static boolean getApplyMonthTermReviewCache(Long teacherId){
        String key = "TeacherAssignTermReview_MonthTermReview_apply_"+teacherId;
        return SafeConverter.toBoolean(teacherScholarshipRecordCache.load(key));
    }

    private static String ckTeacherAssignTermReview(Long teacherId) {
        return "TeacherAssignTermReview" + "_" + teacherId ;
    }

    public static void addAllDayScholarshipTeachers(Set<Long> scholarshipTeachers) {
        Set<Long> datas = loadAllDayScholarshipTeachers();
        if(Objects.nonNull(datas)){
            datas.addAll(scholarshipTeachers);
        }else{
            datas = scholarshipTeachers;
        }
        teacherScholarshipRecordCache.set("AllDayScholarshopTeachers",  (int) (TERMREVIEW_CACHE_END_DATE.getTime() / 1000),datas);
    }

    public static Set<Long> loadAllDayScholarshipTeachers() {
        Set<Long> datas = teacherScholarshipRecordCache.load("AllDayScholarshopTeachers");
        return datas;
    }

    public static void setDayScholarshipTeachers(List<Map<String,Object>> dayScholarshipTeachers) {
        Date todayEnd = DateUtils.getTodayEnd();//有效期到当天结束
        teacherScholarshipRecordCache.set("DayScholarshopTeachers",  (int) (todayEnd.getTime() / 1000),dayScholarshipTeachers);
    }

    public static List<Map<String,Object>> loadDayScholarshipTeachers() {
        List<Map<String,Object>> datas = teacherScholarshipRecordCache.load("DayScholarshopTeachers");
        return datas;
    }


    /**
     * 统计申请每周申请奖励的老师人数
     */
    public static void incrApplyWeekTermReviewTeacher(int curWeek) {
        teacherScholarshipRecordCache.incr("ApplyWeekTermReviewTeachers:"+curWeek,1,1,(int)(TERMREVIEW_CACHE_END_DATE.getTime()/1000));
    }

    public static Integer loadApplyWeekTermReviewTeacher(int curWeek) {
        return SafeConverter.toInt(teacherScholarshipRecordCache.load("ApplyWeekTermReviewTeachers:"+curWeek));
    }

    /**
     * 统计申请整个活动申请奖励的老师人数
     */
    public static void incrApplyMonthTermReviewTeacher() {
        teacherScholarshipRecordCache.incr("ApplyMonthTermReviewTeachers",1,1,(int)(TERMREVIEW_CACHE_END_DATE.getTime()/1000));
    }

    public static Integer loadApplyMonthTermReviewTeacher() {
        return SafeConverter.toInt(teacherScholarshipRecordCache.load("ApplyMonthTermReviewTeachers"));
    }


    public static void updateRecords(List<TeacherAssignTermReviewRecord> records, Long teacherId) {
        teacherScholarshipRecordCache.set(ckTeacherAssignTermReview(teacherId), (int) (TERMREVIEW_CACHE_END_DATE.getTime() / 1000),records);
    }
}
