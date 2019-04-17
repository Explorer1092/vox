/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.business.api.constant.ActivitySchoolLevel;
import com.voxlearning.utopia.business.api.constant.CityLevel;
import com.voxlearning.utopia.business.api.constant.LevelOfSchool;
import com.voxlearning.utopia.entity.activity.TangramActivityStudent;
import com.voxlearning.utopia.entity.activity.TeacherActivityProgress;
import com.voxlearning.utopia.entity.activity.TeacherNewTermActivityProgress;
import com.voxlearning.utopia.entity.activity.TeacherScholarshipRecord;
import com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerActivityRecord;
import com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerWeeklyHomeworkReport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 老师端活动
 */
@ServiceVersion(version = "20181204")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface TeacherActivityService extends IPingable {

    //-------------------------------------------------------------------------
    //---------------------     活动一 - 老师带新认证学生      ------------------
    //-------------------------------------------------------------------------

    @CacheMethod(type = TeacherActivityProgress.class, writeCache = false)
    TeacherActivityProgress findTeacherProgress(Long teacherId);

    @CacheMethod(type = TeacherNewTermActivityProgress.class, writeCache = false)
    TeacherNewTermActivityProgress findTeacherActivityProgress(@CacheParameter("A") Long activityId, @CacheParameter("T") Long teacherId);

    LevelOfSchool getSchoolLevel(Long schoolId);

    @Deprecated
    CityLevel getCityLevel(Integer region);

    CityLevel getCityLevelNew(Integer region,Long activityId);

    ActivitySchoolLevel loadActivitySchoolLevel(Long activityId, Long schoolId);

    MapMessage participate(Long schoolId, Long teacherId);

    MapMessage participateActivity(Long activityId, Long schoolId, Long teacherId);

    @CacheMethod(type = TeacherActivityProgress.class, key = "top100", writeCache = false)
    List<TeacherActivityProgress> loadRankTop100();

    @CacheMethod(type = TeacherNewTermActivityProgress.class, writeCache = false)
    List<TeacherNewTermActivityProgress> loadRankTop100ByActivity(@CacheParameter("top100") Long activityId);

    MapMessage updateActivityProgress(TeacherActivityProgress progress);

    MapMessage updateTeacherActivityProgress(TeacherNewTermActivityProgress progress);

    /**
     * Only For Job
     */
    List<Long> loadAllParticipateTeacherId();

    List<Long> loadAllParticipateTeacherIdByActivity(Long activityId);

    /**
     * Only For Job Too
     */
    MapMessage updateProgressRank(Long id, Integer rank);

    MapMessage updateActivityProgressRank(Long id, Integer rank);

    /**
     * Only For Job Three
     */
    List<Long> loadIDByRankTop10000();

    List<Long> loadIDByRankTop100(Long activityId);

    /**
     * Only For Job Four
     */
    @NoResponseWait
    void clearRankTop100Cache();

    void clearRankTop100Cache(Long activity);

    //-------------------------------------------------------------------------
    //--------------------     活动二 - 数学老师每周作业任务      ----------------
    //-------------------------------------------------------------------------
    Boolean checkTuckerSchool(Long schoolId);

    @CacheMethod(type = TuckerWeeklyHomeworkReport.class, writeCache = false)
    TuckerWeeklyHomeworkReport findTuckerWeeklyHomeworkReport(Long teacherId);

    @CacheMethod(type = TuckerActivityRecord.class, writeCache = false)
    TuckerActivityRecord findTuckerActivityRecord(Long teacherId);

    MapMessage participateTuckerActivity(Long schoolId, Long teacherId);

    MapMessage updateTuckerActivityRecord(TuckerActivityRecord record);

    TuckerWeeklyHomeworkReport loadTeacherLastWeekReport(Long teacherId);

    MapMessage saveTuckerWeeklyHomeworkReport(TuckerWeeklyHomeworkReport report);

    /**
     * Only For Job
     */
    List<TuckerActivityRecord> loadAllTuckerParticipateTeacher();

    // ------------ 奖学金活动 --------------------

    TeacherScholarshipRecord loadTeacherScholarshipRecord(Long teacherId);

    MapMessage updateTeacherScholarshipRecord(TeacherScholarshipRecord record);

    MapMessage applyDailyScholarshipEntrance(Long teacherId);

    MapMessage applyFinalScholarshipEntrance(Long teacherId);

    List<Map<String, Object>> loadScholarshipDailyList();

    Long loadScholarshipFinalAttendNum();

    //==========================================================
    //===============         七巧板活动        =================
    //==========================================================

    @CacheMethod(type = TangramActivityStudent.class, writeCache = false)
    List<TangramActivityStudent> loadTangramTeacherStudents(@CacheParameter("T") Long teacherId);

    @CacheMethod(type = TangramActivityStudent.class, writeCache = false)
    List<TangramActivityStudent> loadTangramSchoolStudents(@CacheParameter("S") Long schoolId);

    TangramActivityStudent loadTangramStudent(Long studentId);

    /**
     * 七巧板活动-新增学生信息
     *
     * @param teacherId    老师ID
     * @param studentName  学生姓名
     * @param studentCode  学生编码
     * @param className    班级名称
     * @param masterpieces 作品列表
     */
    MapMessage addTangramStudent(Long teacherId,
                                 String studentName,
                                 String studentCode,
                                 String className,
                                 List<String> masterpieces);

    /**
     * 七巧板活动-修改学生信息
     *
     * @param studentId    学生ID
     * @param studentName  学生姓名
     * @param studentCode  学生编码
     * @param className    班级名称
     * @param masterpieces 作品列表
     */
    MapMessage modifyTangramStudent(Long studentId,
                                    String studentName,
                                    String studentCode,
                                    String className,
                                    List<String> masterpieces);

    /**
     * 七巧板活动-删除学生信息
     *
     * @param studentId 学生ID
     */
    MapMessage deleteTangramStudent(Long studentId);


    /**
     * 七巧板活动-评审打分
     *
     * @param studentId 学生ID
     * @param score     评分
     * @param comment   点评
     */
    MapMessage judgeTangramStudent(Long studentId, String auditor, String score, String comment);

    MapMessage applyDailyScholarship(Long teacherId);

    MapMessage applyWeekScholarship(Long teacherId);

    MapMessage applyFinalScholarship(Long teacherId);

    boolean updateDailyLottery();

    boolean updateWeekLottery();

}