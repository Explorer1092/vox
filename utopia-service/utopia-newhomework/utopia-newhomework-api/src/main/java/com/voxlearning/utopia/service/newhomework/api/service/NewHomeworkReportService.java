/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.*;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author tanguohong on 2016/1/20.
 */


@ServiceVersion(version = "20190215")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface NewHomeworkReportService extends IPingable {

    List<DisplayStudentHomeWorkHistoryMapper> loadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate);


    Page<DisplayStudentHomeWorkHistoryMapper> loadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate, Pageable pageable);

    MapMessage loadMentalArithmeticChart(String homeworkId, Long studentId);

    MapMessage loadStudentNewHomeworkHistoryDetail(String homeworkId, Long studentId);


    List<Map<String, Object>> loadTeacherUncheckedHomeworkList(Teacher teacher);

    Page<Map<String, Object>> pageHomeworkReportListByGroupIds(Collection<Long> groupIds, Pageable pageable, Subject subject);

    //选择的方式是新加一套接口，减少耦合性
    //接口数据查询不走缓存
    Page<Map<String, Object>> pageHomeworkReportListByGroupIds(Collection<Long> groupIds, Pageable pageable, Subject subject, Date begin, Date end);

    Page<Map<String, Object>> pageHomeworkReportListByGroupIdsAndHomeworkStatus(Collection<Long> groupIds, Pageable pageable, Subject subject, HomeworkStatus homeworkStatus);

    // 包班制支持
    Page<Map<String, Object>> pageHomeworkReportListByGroupIdsAndHomeworkStatus(Collection<Long> groupIds, Pageable pageable, Collection<Subject> subjects, HomeworkStatus homeworkStatus);

    MapMessage homeworkReportForStudent(Teacher teacher, String homeworkId, boolean isPcWay);


    List<Map<String, Object>> homeworkReportForStudentInfo(String homeworkId);


    MapMessage reportDetailIndex(Teacher teacher, String homeworkId);

    MapMessage fetchClazzInfo(List<String> homeworkIds);

    NewHomeworkShareReport processNewHomeworkShareReport(String newHomeworkId, User user, String cdnUrl);

    MapMessage getExcellentDubbingStudent(String newHomeworkId);

    MapMessage loadNewHomeworkReportExamErrorRates(String homeworkId, Long teacherId, boolean isPcWay);

    /**
     * 查询某个学生的作业报告
     */
    MapMessage loadNewHomeworkReportExamErrorRates(String homeworkId, Long studentId, Long teacherId);

    /**
     * 去批改
     * 作业中待批改的部分
     */
    MapMessage loadNewHomeworkNeedCorrect(String homeworkId, Long teacherId);

    /**
     * 基础类型详情：班
     */
    MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, ObjectiveConfigType objectiveConfigType);

    /**
     * 基础类型详情：个人
     */
    MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, Long studentId, ObjectiveConfigType objectiveConfigType);

    MapMessage examAndQuizDetailInfo(String questionId, String homeworkId, ObjectiveConfigType type);

    MapMessage homeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType, StudentDetail studentDetail);

    MapMessage vacationHomeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType);

    MapMessage personalReadingDetail(String homeworkId, Long studentId, String readingId, Long teacherId, ObjectiveConfigType type);

    MapMessage personalDubbingDetail(String homeworkId, Long studentId, String dubbingId, Long teacherId);

    MapMessage personalDubbingWithScoreDetail(String homeworkId, Long studentId, String dubbingId, Long teacherId);

    MapMessage personalOralCommunicationDetail(String homeworkId, Long studentId, String stoneId, Long teacherId);

    MapMessage studentDubbingWithScoreDetail(String homeworkId, Long studentId, String dubbingId);

    MapMessage loadEnglishHomeworkVoiceList(String homeworkId);

    /**
     * 给BASIC_APP提供的接口
     */
    Map<String, Map<String, Object>> lessonDataForBasicApp(NewHomeworkResult newHomeworkResult, List<NewHomeworkApp> apps, Boolean flag, ObjectiveConfigType objectiveConfig);


    JztReport buildNewHomeworkReportV1(NewHomeworkResult newHomeworkResult, User parent, NewHomework newHomework, StudentDetail studentDetail);

    MapMessage semesterChildren(User parent);

    @CacheMethod(type = SemesterReport.class)
    SemesterReport semesterReport(@CacheParameter("S") Long studentId, @CacheParameter("S") String subject);


    //提供给马龙近期作业接口
    String fetchStudentNewestUnfinishedHomework(Long studentId, Collection<Long> groupIds);


    //作业报告按照类型查看接口==>h5
    MapMessage fetchNewHomeworkCommonObjectiveConfigTypePart(Teacher teacher, String hid, ObjectiveConfigType objectiveConfigType, ObjectiveConfigTypeParameter parameter);

    //作业报告单题信息接口==>h5
    MapMessage fetchNewHomeworkSingleQuestionPart(Teacher teacher, String hid, ObjectiveConfigType objectiveConfigType, String qid, String stoneDataId);

    //作业报告按学生查看接口==>app
    MapMessage fetchAppNewHomeworkStudentDetail(String hid, Teacher teacher);


    //作业报告未完成学生名单==>app
    MapMessage fetchAppNewHomeworkUnFinishStudentDetail(String hid, Teacher teacher);


    //作业报告未订正学生名单==>app
    MapMessage fetchAppNewHomeworkUnCorrectStudentDetail(String hid, Teacher teacher);


    //作业报告列表详情接口==>h5
    MapMessage fetchAppNewHomeworkStudentDetailOpenTable(String hid, Teacher teacher);


    //作业报告按题显示接口==>app
    MapMessage fetchAppNewHomeworkTypeQuestion(String hid, Teacher teacher, String cdnBaseUrl);


    //朗读背诵单个App的数据接口==>h5
    MapMessage fetchReadReciteQuestionBoxIdDetail(String hid, String questionBoxId, ObjectiveConfigType type);


    //催促接口==>h5
    MapMessage urgeNewHomework(String hid, Teacher teacher, Set<Long> sids, boolean isCorrect);


    //分享作业报告接口==>h5
    MapMessage shareReport(Teacher teacher, String hid, List<VoiceRecommend.RecommendVoice> recommendVoiceList, List<VoiceRecommend.ReadReciteVoice> readReciteVoiceList,
                           String shareList, List<BaseVoiceRecommend.DubbingWithScore> excellentDubbingStu, List<BaseVoiceRecommend.ImageText> imageTextList);

    //按学生查看接口==》pc
    MapMessage fetchStudentDetailPart(Teacher teacher, String homeworkId);

    //按题目查看接口==>pc
    MapMessage fetchQuestionDetailPart(Teacher teacher, String homeworkId, String cdnBaseUrl);


    //新朗读背诵单个app个人报告
    MapMessage personalReadReciteWithScore(String hid, String questionBoxId, Long sid);


    MapMessage fetchPictureBookPlusDubbing(String dubbingId);

    //生字认读单个app个人报告
    MapMessage personalWordRecognitionAndReading(String hid, String questionBoxId, Long sid);

    //纸质口算个人报告
    MapMessage personalOcrMentalArithmetic(String hid, String ocrAnswers, Long sid);

    //家长通学生作业报告
    JztStudentHomeworkReport loadJztStudentHomeworkReport(NewHomeworkResult newHomeworkResult, NewHomework newHomework, StudentDetail studentDetail);

    //家长通班级作业报告
    JztClazzHomeworkReport loadJztClazzHomeworkReport(NewHomework newHomework, StudentDetail studentDetail, String cdnUrl);

    //家长通作业通知
    JztHomeworkNotice loadJztHomeworkNotice(NewHomework newHomework, StudentDetail studentDetail, String cdnUrl, Long parentId);

    // 诊断做题习惯详情
    MapMessage loadDiagnosisHabitDetail(String homeworkId);

    // 字词讲练模块班级详情
    MapMessage clazzWordTeachModuleDetail(Long teacherId, String hid, String stoneId, WordTeachModuleType wordTeachModuleType);

    // 字词讲练图文入韵模块个人详情
    MapMessage studentImageTextRhymeDetail(String homeworkId, Long studentId, String stoneDataId, String chapterId);

    // 纸质作业报告详情
    MapMessage fetchOcrHomeworkStudentDetail(String hid, Teacher teacher);

    // 纸质作业报告分享
    OcrHomeworkShareReport processOcrHomeworkShareReport(String newHomeworkId, User user, String cdnUrl);

    // 纸质作业作业单详情
    MapMessage loadOcrHomeworkDetail(List<String> homeworkIds);
}
