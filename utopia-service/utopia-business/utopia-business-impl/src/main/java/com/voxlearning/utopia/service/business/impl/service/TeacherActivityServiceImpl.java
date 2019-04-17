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

package com.voxlearning.utopia.service.business.impl.service;

import com.lambdaworks.redis.api.sync.RedisHashCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.support.MongoExceptionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.business.api.TeacherActivityService;
import com.voxlearning.utopia.business.api.constant.ActivitySchoolLevel;
import com.voxlearning.utopia.business.api.constant.CityLevel;
import com.voxlearning.utopia.business.api.constant.LevelOfSchool;
import com.voxlearning.utopia.entity.activity.TangramActivityStudent;
import com.voxlearning.utopia.entity.activity.TeacherActivityProgress;
import com.voxlearning.utopia.entity.activity.TeacherNewTermActivityProgress;
import com.voxlearning.utopia.entity.activity.TeacherScholarshipRecord;
import com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerActivityRecord;
import com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerWeeklyHomeworkReport;
import com.voxlearning.utopia.service.business.impl.dao.*;
import com.voxlearning.utopia.service.business.impl.persistence.TangramActivityStudentDao;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.business.impl.support.NewTermActivityCityDictionary;
import com.voxlearning.utopia.service.business.impl.support.NewTermActivitySchoolDictionary;
import com.voxlearning.utopia.service.business.impl.support.NewTermActivitySchoolLevelDictionary;
import com.voxlearning.utopia.service.config.api.util.BadWordChecker;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.business.api.constant.TeacherNewTermActivityCategory.JuniorEnglishStudent;

@Named
@ExposeService(interfaceClass = TeacherActivityService.class)
public class TeacherActivityServiceImpl extends BusinessServiceSpringBean implements TeacherActivityService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private TeacherActivityProgressPersistence teacherActivityProgressPersistence;
    @Inject private TeacherNewTermActivityProgressPersistence teacherNewTermActivityProgressPersistence;
    @Inject private TeacherScholarShipRecordDao teacherScholarShipRecordDao;
    @Inject private TangramActivityStudentDao tangramActivityStudentDao;
    @Inject private TuckerActivityRecordDao tuckerActivityRecordDao;
    @Inject private TuckerWeeklyHomeworkReportDao tuckerWeeklyHomeworkReportDao;

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() {
        this.redisCommands = RedisCommandsBuilder.getInstance().getRedisCommands("user-easemob");
    }

    @Override
    public TeacherActivityProgress findTeacherProgress(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        return teacherActivityProgressPersistence.load(teacherId);
    }

    @Override
    public TeacherNewTermActivityProgress findTeacherActivityProgress(Long activityId, Long teacherId) {
        if (activityId == null || teacherId == null) {
            return null;
        }
        return teacherNewTermActivityProgressPersistence.loadProgress(activityId, teacherId);
    }

    @Override
    public LevelOfSchool getSchoolLevel(Long schoolId) {
        if (schoolId == null) {
            return null;
        }
        // 测试环境根据 学校ID 确定学校等级
        if (RuntimeMode.isUsingTestData()) {
            String level = String.valueOf((char) ('A' + (schoolId % 10) / 2));
            return "D".equals(level) ? null : LevelOfSchool.parse(level);
        }
        return NewTermActivitySchoolDictionary.getSchoolLevel(schoolId);
    }

    @Override
    @Deprecated
    public CityLevel getCityLevel(Integer region) {
        return getCityLevelNew(region, JuniorEnglishStudent.getId());
    }

    @Override
    public CityLevel getCityLevelNew(Integer region, Long activityId) {
        if (region == null) {
            return null;
        }
        ExRegion exRegion = raikouSystem.loadRegion(region);
        if (exRegion == null || exRegion.getCityCode() <= 0) {
            return null;
        }
        return NewTermActivityCityDictionary.getCityLevel(exRegion.getCityCode(), activityId);
    }

    @Override
    public ActivitySchoolLevel loadActivitySchoolLevel(Long activityId, Long schoolId) {
        if (activityId == null || schoolId == null) {
            return null;
        }
        String level = NewTermActivitySchoolLevelDictionary.getSchoolLevel(activityId, schoolId);
        // 测试环境根据 学校ID 确定学校等级
        if (RuntimeMode.isUsingTestData()) {
            level = String.valueOf((char) ('A' + (schoolId % 10) / 2));
        }
        if (level == null) {
            return null;
        }
        return ActivitySchoolLevel.loadActivitySchoolLevel(activityId, level);
    }

    @Override
    public MapMessage participate(Long schoolId, Long teacherId) {
        if (schoolId == null || teacherId == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        TeacherActivityProgress progress = teacherActivityProgressPersistence.load(teacherId);
        if (progress != null) {
            return MapMessage.successMessage();
        }
        progress = new TeacherActivityProgress(teacherId);
        progress.setSchoolId(schoolId);
        if (SafeConverter.toInt(progress.getRank()) == 0) progress.setRank(99999);
        try {
            teacherActivityProgressPersistence.insert(progress);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed participate term 2017 activity, teacherId={}", teacherId, ex);
            return MapMessage.errorMessage("网络连接不稳定，请稍后刷新重试");
        }
    }

    @Override
    public MapMessage participateActivity(Long activityId, Long schoolId, Long teacherId) {
        if (schoolId == null || teacherId == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        TeacherNewTermActivityProgress progress = teacherNewTermActivityProgressPersistence.loadProgress(activityId, teacherId);
        if (progress != null) {
            return MapMessage.successMessage();
        }
        progress = new TeacherNewTermActivityProgress(activityId, teacherId);
        progress.setSchoolId(schoolId);
        if (SafeConverter.toInt(progress.getRank()) == 0) progress.setRank(99999);
        try {
            teacherNewTermActivityProgressPersistence.insert(progress);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed participate term activity, activityId={}, teacherId={}", activityId, teacherId, ex);
            return MapMessage.errorMessage("网络连接不稳定，请稍后刷新重试");
        }
    }

    @Override
    public List<TeacherActivityProgress> loadRankTop100() {
        return teacherActivityProgressPersistence.loadTop100ByRank();
    }

    @Override
    public List<TeacherNewTermActivityProgress> loadRankTop100ByActivity(Long activityId) {
        return teacherNewTermActivityProgressPersistence.loadTop100ByRank(activityId);
    }

    @Override
    public List<Long> loadAllParticipateTeacherId() {
        return teacherActivityProgressPersistence.loadAllTeacherId();
    }

    @Override
    public List<Long> loadAllParticipateTeacherIdByActivity(Long activityId) {
        return teacherNewTermActivityProgressPersistence.loadAllTeacherId(activityId);
    }

    @Override
    public MapMessage updateActivityProgress(TeacherActivityProgress progress) {
        if (progress == null) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            teacherActivityProgressPersistence.upsert(progress);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed update TeacherActivityProgress, id={}", progress.getId(), ex);
            return MapMessage.errorMessage("更新失败");
        }
    }

    @Override
    public MapMessage updateTeacherActivityProgress(TeacherNewTermActivityProgress progress) {
        if (progress == null) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            teacherNewTermActivityProgressPersistence.upsert(progress);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed update TeacherNewTermActivityProgress, id={}", progress.getId(), ex);
            return MapMessage.errorMessage("更新失败");
        }
    }

    @Override
    public MapMessage updateProgressRank(Long id, Integer rank) {
        if (id == null || rank == null) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            int rows = teacherActivityProgressPersistence.updateRank(id, rank);
            return MapMessage.successMessage().add("rows", rows);
        } catch (Exception ex) {
            logger.error("Failed update TeacherActivityProgress rank, id={}, rank={}", id, rank, ex);
            return MapMessage.errorMessage("更新失败");
        }
    }

    @Override
    public MapMessage updateActivityProgressRank(Long id, Integer rank) {
        if (id == null || rank == null) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            int rows = teacherNewTermActivityProgressPersistence.updateRank(id, rank);
            return MapMessage.successMessage().add("rows", rows);
        } catch (Exception ex) {
            logger.error("Failed update TeacherNewTermActivityProgress rank, id={}, rank={}", id, rank, ex);
            return MapMessage.errorMessage("更新失败");
        }
    }

    @Override
    public List<Long> loadIDByRankTop10000() {
        return teacherActivityProgressPersistence.loadRankTop10000();
    }

    @Override
    public List<Long> loadIDByRankTop100(Long activityId) {
        return teacherNewTermActivityProgressPersistence.loadRankTop100(activityId);
    }

    @Override
    public void clearRankTop100Cache() {
        teacherActivityProgressPersistence.clearRankTop100Cache();
    }

    @Override
    public void clearRankTop100Cache(Long activity) {
        teacherNewTermActivityProgressPersistence.clearRankTop100Cache(activity);
    }

    @Override
    public Boolean checkTuckerSchool(Long schoolId) {
        if (schoolId == null || schoolId <= 0L) {
            return Boolean.FALSE;
        }
        if (RuntimeMode.isUsingTestData()) {
            return !String.valueOf(schoolId).endsWith("0");
        }
        return NewTermActivitySchoolDictionary.checkTuckerSchool(schoolId);
    }

    @Override
    public TuckerWeeklyHomeworkReport findTuckerWeeklyHomeworkReport(Long teacherId) {
        return tuckerWeeklyHomeworkReportDao.load(teacherId);
    }

    @Override
    public TuckerActivityRecord findTuckerActivityRecord(Long teacherId) {
        return tuckerActivityRecordDao.loadByTeacherId(teacherId);
    }

    @Override
    public MapMessage participateTuckerActivity(Long schoolId, Long teacherId) {
        if (schoolId == null || teacherId == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        TuckerActivityRecord record = tuckerActivityRecordDao.loadByTeacherId(teacherId);
        if (record != null) {
            return MapMessage.successMessage();
        }
        record = new TuckerActivityRecord(teacherId);
        record.setSchoolId(schoolId);
        try {
            tuckerActivityRecordDao.insert(record);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed participate Tucker Activity, teacherId={}", teacherId, ex);
            return MapMessage.errorMessage("网络连接不稳定，请稍后刷新重试");
        }
    }

    @Override
    public MapMessage updateTuckerActivityRecord(TuckerActivityRecord record) {
        if (record == null) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            tuckerActivityRecordDao.upsert(record);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed update TuckerActivityRecord, id={}", record.getId(), ex);
            return MapMessage.errorMessage("更新失败");
        }
    }

    @Override
    public TuckerWeeklyHomeworkReport loadTeacherLastWeekReport(Long teacherId) {
        return tuckerWeeklyHomeworkReportDao.loadLastWeekReport(teacherId);
    }

    @Override
    public MapMessage saveTuckerWeeklyHomeworkReport(TuckerWeeklyHomeworkReport report) {
        try {
            tuckerWeeklyHomeworkReportDao.upsert(report);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            if (MongoExceptionUtils.isDuplicateKeyError(ex)) {
                return MapMessage.successMessage();
            }
            logger.error("Failed ");
            return MapMessage.errorMessage();
        }
    }

    @Override
    public List<TuckerActivityRecord> loadAllTuckerParticipateTeacher() {
        return tuckerActivityRecordDao.query();
    }

    @Override
    public TeacherScholarshipRecord loadTeacherScholarshipRecord(Long teacherId) {
        return teacherScholarShipRecordDao.loadByTeacherId(teacherId);
    }

    /**
     * 获得老师最近一次布置作业的日期，考虑包班制
     */
    private Date getTeacherLastSignDateInRecord(Long teacherId) {
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId == null || mainTeacherId == 0L)
            mainTeacherId = teacherId;

        List<Long> teacherIds = new ArrayList<>();
        teacherIds.add(mainTeacherId);
        teacherIds.addAll(teacherLoaderClient.loadSubTeacherIds(mainTeacherId));

        return teacherIds.stream().map(this::loadTeacherScholarshipRecord)
                .filter(Objects::nonNull)
                .map(TeacherScholarshipRecord::getLastAssignDate)
                .filter(Objects::nonNull).min(Comparator.reverseOrder())
                .orElse(null);
    }

    @Override
    public MapMessage updateTeacherScholarshipRecord(TeacherScholarshipRecord record) {
        try {
            teacherScholarShipRecordDao.upsert(record);
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage applyDailyScholarshipEntrance(Long teacherId) {
        Date todayBegin = DateUtils.truncate(new Date(), Calendar.DATE);
        Date lastAssignDate = getTeacherLastSignDateInRecord(teacherId);
        if (lastAssignDate == null || todayBegin.after(lastAssignDate)) {
            return MapMessage.errorMessage("今天未布置作业").add("records", false);
        }

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherScholarship:apply")
                    .keys(teacherId)
                    .callback(() -> {
                        TeacherScholarshipRecord record = loadTeacherScholarshipRecord(teacherId);
                        if (record == null) {
                            record = new TeacherScholarshipRecord();
                            record.setTeacherId(teacherId);
                        }

                        // 不能反复领取
                        if (SafeConverter.toBoolean(record.getDailyLottery()))
                            return MapMessage.errorMessage("不能重复领取!");
                        else {
                            record.setDailyLottery(true);
                            teacherScholarShipRecordDao.upsert(record);

                            IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.TEACHER_SCHOLARSHIP_ACTIVITY_REWARD, 10);
                            integralHistory.setComment("参与奖学金活动获得奖励");
                            MapMessage incIntegralMsg = userIntegralService.changeIntegral(integralHistory);
                            if (!incIntegralMsg.isSuccess()) {
                                return MapMessage.errorMessage("领取失败!增加园丁豆失败!" + incIntegralMsg.getInfo());
                            }

                            return MapMessage.successMessage();
                        }
                    })
                    .build()
                    .execute();
        } catch (Exception e) {
            return MapMessage.errorMessage("领取失败!");
        }
    }

    @Override
    public MapMessage applyFinalScholarshipEntrance(Long teacherId) {
        return AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("TeacherScholarship:upsert")
                .keys(teacherId)
                .callback(() -> internalUpdateFinalLotteryFlag(teacherId))
                .build()
                .execute();
    }

    private MapMessage internalUpdateFinalLotteryFlag(Long teacherId) {
        TeacherScholarshipRecord record = loadTeacherScholarshipRecord(teacherId);
        if (record == null) {
            record = new TeacherScholarshipRecord();
            record.setTeacherId(teacherId);
        }

        record.setFinalLottery(true);
        return updateTeacherScholarshipRecord(record);
    }

    @Override
    public List<Map<String, Object>> loadScholarshipDailyList() {
        RedisHashCommands<String, Object> hashCommands = this.redisCommands.sync().getRedisHashCommands();
        Map<String, Object> listMapByDate = hashCommands.hgetall("TeacherScholarshipActivity:DailyAwards");

        final String dateFormat = "yyyy-MM-dd";
        Function<Map<String, Object>, Date> dateFunc = m -> DateUtils.stringToDate(MapUtils.getString(m, "date"), dateFormat);
        Date startDate = DateUtils.stringToDate("2018-05-01", dateFormat);

        return listMapByDate.values()
                .stream()
                .map(t -> (Map<String, Object>) t)
                .filter(m -> dateFunc.apply(m).after(startDate))
                .sorted((n1, n2) -> {
                    Date d1 = dateFunc.apply(n1);
                    Date d2 = dateFunc.apply(n2);

                    if (d1 == null || d2 == null)
                        return 0;

                    return d2.compareTo(d1);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long loadScholarshipFinalAttendNum() {
        RedisStringCommands<String, Object> commands = redisCommands.sync().getRedisStringCommands();
        return commands.bitcount("TeacherScholarshipActivity:FinalAttendNum");
    }


    public MapMessage updateTeacherScholarshipVariable(Long teacherId,
                                                       int termReviewNumDelta,
                                                       int basicReviewNumDelta,
                                                       double finishRate,
                                                       double score) {

        boolean result = teacherScholarShipRecordDao.updateTeacherRecordVariable(
                teacherId,
                termReviewNumDelta,
                basicReviewNumDelta,
                finishRate,
                score);

        if (result)
            return MapMessage.successMessage();

        return MapMessage.errorMessage("更新失败!");
    }

    //==========================================================
    //===============         七巧板活动        =================
    //==========================================================

    @Override
    public List<TangramActivityStudent> loadTangramTeacherStudents(Long teacherId) {
        if (teacherId == null || teacherId <= 0L) {
            return Collections.emptyList();
        }
        return tangramActivityStudentDao.findByTeacher(teacherId);
    }

    @Override
    public List<TangramActivityStudent> loadTangramSchoolStudents(Long schoolId) {
        if (schoolId == null || schoolId <= 0L) {
            return Collections.emptyList();
        }
        return tangramActivityStudentDao.findBySchool(schoolId);
    }

    @Override
    public TangramActivityStudent loadTangramStudent(Long studentId) {
        if (studentId == null || studentId <= 0L) {
            return null;
        }
        return tangramActivityStudentDao.load(studentId);
    }

    @Override
    public MapMessage addTangramStudent(Long teacherId,
                                        String studentName,
                                        String studentCode,
                                        String className,
                                        List<String> masterpieces) {
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师信息");
        }

        int limit = TangramActivityStudent.STUDENT_MAXIMUM_LIMIT;
        if (RuntimeMode.le(Mode.STAGING)) limit = 5;
        List<TangramActivityStudent> studentList = tangramActivityStudentDao.findByTeacher(teacherId);
        if (studentList.size() >= limit) {
            return MapMessage.errorMessage("已上传{}名学生作品，达到数量上限", limit);
        }

        if (masterpieces != null && masterpieces.size() > TangramActivityStudent.MASTERPIECE_MAXIMUM_LIMIT) {
            return MapMessage.errorMessage("每个学生最多只能上传{}幅作品", TangramActivityStudent.MASTERPIECE_MAXIMUM_LIMIT);
        }

        // 保存学生信息
        TangramActivityStudent student = TangramActivityStudent.newInstance(
                teacher.getTeacherSchoolId(), teacherId, studentName, studentCode, className, masterpieces
        );
        MapMessage checkMsg = student.check();
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        tangramActivityStudentDao.insert(student);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage modifyTangramStudent(Long studentId,
                                           String studentName,
                                           String studentCode,
                                           String className,
                                           List<String> masterpieces) {
        TangramActivityStudent student = tangramActivityStudentDao.load(studentId);
        if (student == null || student.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的学生信息");
        }

        if (masterpieces != null && masterpieces.size() > TangramActivityStudent.MASTERPIECE_MAXIMUM_LIMIT) {
            return MapMessage.errorMessage("每个学生最多只能上传{}幅作品", TangramActivityStudent.MASTERPIECE_MAXIMUM_LIMIT);
        }

        student.setStudentName(studentName);
        student.setStudentCode(studentCode);
        student.setClassName(className);
        student.updateMasterpieces(masterpieces);

        MapMessage checkMsg = student.check();
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }

        tangramActivityStudentDao.replace(student);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteTangramStudent(Long studentId) {
        if (studentId == null || studentId <= 0L) {
            return MapMessage.errorMessage("参数错误");
        }

        // 删除学生信息
        tangramActivityStudentDao.disable(studentId);

        return MapMessage.successMessage();
    }


    @Override
    public MapMessage judgeTangramStudent(Long studentId, String auditor, String score, String comment) {
        if (studentId == null || studentId <= 0L || StringUtils.isAnyBlank(auditor, score)) {
            return MapMessage.errorMessage("无效的参数");
        }

        // 检查评分
        TangramActivityStudent.Score scoreVal = TangramActivityStudent.Score.parse(score);
        if (TangramActivityStudent.Score.UNTITLED == scoreVal) {
            return MapMessage.errorMessage("无效的评分");
        }

        // 检查评论
        if (comment == null) comment = "";
        if (comment.length() > 500) {
            return MapMessage.errorMessage("评论请勿超过500字");
        }
        String badWord = BadWordChecker.getConversationChecker().findBadWord(comment);
        if (StringUtils.isNotBlank(badWord)) {
            return MapMessage.errorMessage("评论中有敏感词汇（{}），请酌情调整", badWord);
        }

        // 检查
        TangramActivityStudent student = tangramActivityStudentDao.load(studentId);
        if (student == null || student.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的学生信息");
        }

        student.setScore(score);
        student.setComment(comment);
        student.setAuditor(auditor);
        student.setAuditTime(new Date());

        tangramActivityStudentDao.replace(student);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage applyDailyScholarship(Long teacherId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherDailyScholarship:apply")
                    .keys(teacherId)
                    .callback(() -> {
                        TeacherScholarshipRecord record = loadTeacherScholarshipRecord(teacherId);
                        if (record == null) {
                            MapMessage.errorMessage("领取失败!");
                        }
                        // 不能反复领取
                        if (SafeConverter.toBoolean(record.getDailyLottery()))
                            return MapMessage.errorMessage("不能重复领取!");
                        else {
                            record.setDailyLottery(true);
                            teacherScholarShipRecordDao.upsert(record);
                            return MapMessage.successMessage();
                        }
                    })
                    .build()
                    .execute();
        } catch (Exception e) {
            return MapMessage.errorMessage("领取失败!");
        }
    }

    @Override
    public MapMessage applyWeekScholarship(Long teacherId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherWeekScholarship:apply")
                    .keys(teacherId)
                    .callback(() -> {
                        TeacherScholarshipRecord record = loadTeacherScholarshipRecord(teacherId);
                        if (record == null) {
                            MapMessage.errorMessage("领取失败!");
                        }
                        // 不能反复领取
                        if (SafeConverter.toBoolean(record.getWeekLottery()))
                            return MapMessage.errorMessage("不能重复领取!");
                        else {
                            record.setWeekLottery(true);
                            teacherScholarShipRecordDao.upsert(record);
                            return MapMessage.successMessage();
                        }
                    })
                    .build()
                    .execute();
        } catch (Exception e) {
            return MapMessage.errorMessage("领取失败!");
        }
    }

    @Override
    public MapMessage applyFinalScholarship(Long teacherId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherFinalScholarship:apply")
                    .keys(teacherId)
                    .callback(() -> {
                        TeacherScholarshipRecord record = loadTeacherScholarshipRecord(teacherId);
                        if (record == null) {
                            MapMessage.errorMessage("领取失败!");
                        }
                        // 不能反复领取
                        if (SafeConverter.toBoolean(record.getFinalLottery()))
                            return MapMessage.errorMessage("不能重复领取!");
                        else {
                            record.setFinalLottery(true);
                            teacherScholarShipRecordDao.upsert(record);
                            return MapMessage.successMessage();
                        }
                    })
                    .build()
                    .execute();
        } catch (Exception e) {
            return MapMessage.errorMessage("领取失败!");
        }
    }

    @Override
    public boolean updateDailyLottery() {
        return teacherScholarShipRecordDao.updateDailyLottery();
    }

    @Override
    public boolean updateWeekLottery() {
        return teacherScholarShipRecordDao.updateWeekLottery();
    }

}
