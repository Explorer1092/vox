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

package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.*;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkReportService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.*;

public class NewHomeworkReportServiceClient implements NewHomeworkReportService {

    @ImportService(interfaceClass = NewHomeworkReportService.class)
    private NewHomeworkReportService remoteReference;

    @Override
    public List<DisplayStudentHomeWorkHistoryMapper> loadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate) {
        return remoteReference.loadStudentNewHomeworkHistory(student, startDate, endDate);
    }


    @Override
    public Page<DisplayStudentHomeWorkHistoryMapper> loadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate, Pageable pageable) {

        return remoteReference.loadStudentNewHomeworkHistory(student, startDate, endDate, pageable);
    }

    @Override
    public MapMessage loadMentalArithmeticChart(String homeworkId, Long studentId) {
        return remoteReference.loadMentalArithmeticChart(homeworkId, studentId);
    }

    @Override
    public MapMessage loadStudentNewHomeworkHistoryDetail(String homeworkId, Long studentId) {
        return remoteReference.loadStudentNewHomeworkHistoryDetail(homeworkId, studentId);
    }


    @Override
    public List<Map<String, Object>> loadTeacherUncheckedHomeworkList(Teacher teacher) {
        return remoteReference.loadTeacherUncheckedHomeworkList(teacher);
    }

    @Override
    public Page<Map<String, Object>> pageHomeworkReportListByGroupIds(Collection<Long> groupIds, Pageable pageable, Subject subject) {
        return remoteReference.pageHomeworkReportListByGroupIds(groupIds, pageable, subject);
    }

    @Override
    public Page<Map<String, Object>> pageHomeworkReportListByGroupIds(Collection<Long> groupIds, Pageable pageable, Subject subject, Date begin, Date end) {
        return remoteReference.pageHomeworkReportListByGroupIds(groupIds, pageable, subject, begin, end);
    }

    @Override
    public Page<Map<String, Object>> pageHomeworkReportListByGroupIdsAndHomeworkStatus(Collection<Long> groupIds, Pageable pageable, Subject subject, HomeworkStatus homeworkStatus) {
        return remoteReference.pageHomeworkReportListByGroupIdsAndHomeworkStatus(groupIds, pageable, subject, homeworkStatus);
    }

    @Override
    public Page<Map<String, Object>> pageHomeworkReportListByGroupIdsAndHomeworkStatus(Collection<Long> groupIds, Pageable pageable, Collection<Subject> subjects, HomeworkStatus homeworkStatus) {
        return remoteReference.pageHomeworkReportListByGroupIdsAndHomeworkStatus(groupIds, pageable, subjects, homeworkStatus);
    }

    @Override
    public MapMessage homeworkReportForStudent(Teacher teacher, String homeworkId, boolean isPcWay) {
        return remoteReference.homeworkReportForStudent(teacher, homeworkId, isPcWay);
    }


    @Override
    public List<Map<String, Object>> homeworkReportForStudentInfo(String homeworkId) {
        return remoteReference.homeworkReportForStudentInfo(homeworkId);
    }

    @Override
    public MapMessage fetchStudentDetailPart(Teacher teacher, String homeworkId) {
        return remoteReference.fetchStudentDetailPart(teacher, homeworkId);
    }

    @Override
    public MapMessage fetchQuestionDetailPart(Teacher teacher, String homeworkId, String cdnBaseUrl) {
        return remoteReference.fetchQuestionDetailPart(teacher, homeworkId, cdnBaseUrl);
    }

    @Override
    public MapMessage personalReadReciteWithScore(String hid, String questionBoxId, Long sid) {
        return remoteReference.personalReadReciteWithScore(hid, questionBoxId, sid);
    }

    @Override
    public MapMessage fetchPictureBookPlusDubbing(String dubbingId) {
        return remoteReference.fetchPictureBookPlusDubbing(dubbingId);
    }

    @Override
    public MapMessage personalWordRecognitionAndReading(String hid, String questionBoxId, Long sid) {
        return remoteReference.personalWordRecognitionAndReading(hid,questionBoxId,sid);
    }

    @Override
    public MapMessage personalOcrMentalArithmetic(String hid, String ocrAnswers, Long sid) {
        return remoteReference.personalOcrMentalArithmetic(hid,ocrAnswers,sid);
    }

    @Override
    public MapMessage reportDetailIndex(Teacher teacher, String homeworkId) {
        return remoteReference.reportDetailIndex(teacher, homeworkId);
    }

    @Override
    public MapMessage fetchClazzInfo(List<String> homeworkIds) {
        return remoteReference.fetchClazzInfo(homeworkIds);
    }

    @Override
    public NewHomeworkShareReport processNewHomeworkShareReport(String newHomeworkId, User user, String cdnUrl) {
        return remoteReference.processNewHomeworkShareReport(newHomeworkId, user, cdnUrl);
    }

    @Override
    public MapMessage getExcellentDubbingStudent(String newHomeworkId) {
        return remoteReference.getExcellentDubbingStudent(newHomeworkId);
    }

    @Override
    public MapMessage loadNewHomeworkReportExamErrorRates(String homeworkId, Long teacherId, boolean isPcWay) {
        return remoteReference.loadNewHomeworkReportExamErrorRates(homeworkId, teacherId, isPcWay);
    }

    @Override
    public MapMessage loadNewHomeworkReportExamErrorRates(String homeworkId, Long studentId, Long teacherId) {
        return remoteReference.loadNewHomeworkReportExamErrorRates(homeworkId, studentId, teacherId);
    }

    @Override
    public MapMessage loadNewHomeworkNeedCorrect(String homeworkId, Long teacherId) {
        return remoteReference.loadNewHomeworkNeedCorrect(homeworkId, teacherId);
    }

    @Override
    public MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, ObjectiveConfigType objectiveConfigType) {
        return remoteReference.reportDetailsBaseApp(homeworkId, categoryId, lessonId, objectiveConfigType);
    }

    @Override
    public MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, Long studentId, ObjectiveConfigType objectiveConfigType) {
        return remoteReference.reportDetailsBaseApp(homeworkId, categoryId, lessonId, studentId, objectiveConfigType);
    }

    @Override
    public MapMessage examAndQuizDetailInfo(String questionId, String homeworkId, ObjectiveConfigType type) {
        return remoteReference.examAndQuizDetailInfo(questionId, homeworkId, type);
    }

    @Override
    public MapMessage homeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType, StudentDetail studentDetail) {
        return remoteReference.homeworkForObjectiveConfigTypeResult(homeworkId, objectiveConfigType, studentDetail);
    }

    @Override
    public MapMessage vacationHomeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType) {
        return remoteReference.vacationHomeworkForObjectiveConfigTypeResult(homeworkId, objectiveConfigType);
    }

    @Override
    public MapMessage personalReadingDetail(String homeworkId, Long studentId, String readingId, Long teacherId, ObjectiveConfigType type) {
        return remoteReference.personalReadingDetail(homeworkId, studentId, readingId, teacherId, type);
    }

    @Override
    public MapMessage personalDubbingDetail(String homeworkId, Long studentId, String dubbingId, Long teacherId) {
        return remoteReference.personalDubbingDetail(homeworkId, studentId, dubbingId, teacherId);
    }

    @Override
    public MapMessage personalDubbingWithScoreDetail(String homeworkId, Long studentId, String dubbingId, Long teacherId) {
        return remoteReference.personalDubbingWithScoreDetail(homeworkId, studentId, dubbingId, teacherId);
    }

    @Override
    public MapMessage personalOralCommunicationDetail(String homeworkId, Long studentId, String stoneId, Long teacherId) {
        return remoteReference.personalOralCommunicationDetail(homeworkId,studentId,stoneId,teacherId);
    }

    @Override
    public MapMessage studentDubbingWithScoreDetail(String homeworkId, Long studentId, String dubbingId) {
        return remoteReference.studentDubbingWithScoreDetail(homeworkId,studentId,dubbingId);
    }

    @Override
    public MapMessage loadEnglishHomeworkVoiceList(String homeworkId) {
        return remoteReference.loadEnglishHomeworkVoiceList(homeworkId);
    }

    @Override
    public Map<String, Map<String, Object>> lessonDataForBasicApp(NewHomeworkResult newHomeworkResult, List<NewHomeworkApp> apps, Boolean flag, ObjectiveConfigType objectiveConfig) {
        return remoteReference.lessonDataForBasicApp(newHomeworkResult, apps, flag, objectiveConfig);
    }


    @Override
    public JztReport buildNewHomeworkReportV1(NewHomeworkResult newHomeworkResult, User parent, NewHomework newHomework, StudentDetail studentDetail) {
        return remoteReference.buildNewHomeworkReportV1(newHomeworkResult, parent, newHomework, studentDetail);
    }

    @Override
    public MapMessage semesterChildren(User parent) {
        return remoteReference.semesterChildren(parent);
    }

    @Override
    public SemesterReport semesterReport(Long studentId, String subject) {
        return remoteReference.semesterReport(studentId, subject);
    }

    @Override
    public String fetchStudentNewestUnfinishedHomework(Long studentId, Collection<Long> groupIds) {
        return remoteReference.fetchStudentNewestUnfinishedHomework(studentId, groupIds);
    }


    @Override
    public MapMessage fetchNewHomeworkCommonObjectiveConfigTypePart(Teacher teacher, String hid, ObjectiveConfigType objectiveConfigType, ObjectiveConfigTypeParameter parameter) {
        return remoteReference.fetchNewHomeworkCommonObjectiveConfigTypePart(teacher, hid, objectiveConfigType, parameter);
    }

    @Override
    public MapMessage fetchNewHomeworkSingleQuestionPart(Teacher teacher, String hid, ObjectiveConfigType objectiveConfigType, String qid, String stoneDataId) {
        return remoteReference.fetchNewHomeworkSingleQuestionPart(teacher, hid, objectiveConfigType, qid, stoneDataId);
    }

    @Override
    public MapMessage fetchAppNewHomeworkStudentDetail(String hid, Teacher teacher) {
        return remoteReference.fetchAppNewHomeworkStudentDetail(hid, teacher);
    }


    @Override
    public MapMessage fetchAppNewHomeworkUnFinishStudentDetail(String hid, Teacher teacher) {
        return remoteReference.fetchAppNewHomeworkUnFinishStudentDetail(hid, teacher);
    }

    @Override
    public MapMessage fetchAppNewHomeworkUnCorrectStudentDetail(String hid, Teacher teacher) {
        return remoteReference.fetchAppNewHomeworkUnCorrectStudentDetail(hid, teacher);
    }

    @Override
    public MapMessage fetchAppNewHomeworkStudentDetailOpenTable(String hid, Teacher teacher) {
        return remoteReference.fetchAppNewHomeworkStudentDetailOpenTable(hid, teacher);
    }

    @Override
    public MapMessage fetchAppNewHomeworkTypeQuestion(String hid, Teacher teacher, String cdnBaseUrl) {
        return remoteReference.fetchAppNewHomeworkTypeQuestion(hid, teacher, cdnBaseUrl);
    }

    @Override
    public MapMessage fetchReadReciteQuestionBoxIdDetail(String hid, String questionBoxId, ObjectiveConfigType type) {
        return remoteReference.fetchReadReciteQuestionBoxIdDetail(hid, questionBoxId, type);
    }

    @Override
    public MapMessage urgeNewHomework(String hid, Teacher teacher, Set<Long> sids, boolean isCorrect) {
        return remoteReference.urgeNewHomework(hid, teacher, sids, isCorrect);
    }

    @Override
    public MapMessage shareReport(Teacher teacher, String hid, List<VoiceRecommend.RecommendVoice> recommendVoiceList, List<VoiceRecommend.ReadReciteVoice> readReciteVoiceList,
                                  String shareList, List<BaseVoiceRecommend.DubbingWithScore> excellentDubbingStu, List<BaseVoiceRecommend.ImageText> imageTextList) {
        return remoteReference.shareReport(teacher, hid, recommendVoiceList, readReciteVoiceList, shareList,excellentDubbingStu, imageTextList);
    }

    @Override
    public JztStudentHomeworkReport loadJztStudentHomeworkReport(NewHomeworkResult newHomeworkResult, NewHomework newHomework, StudentDetail studentDetail) {
        return remoteReference.loadJztStudentHomeworkReport(newHomeworkResult, newHomework, studentDetail);
    }

    @Override
    public JztClazzHomeworkReport loadJztClazzHomeworkReport(NewHomework newHomework, StudentDetail studentDetail, String cdnUrl){
        return remoteReference.loadJztClazzHomeworkReport(newHomework, studentDetail, cdnUrl);
    }

    @Override
    public JztHomeworkNotice loadJztHomeworkNotice(NewHomework newHomework, StudentDetail studentDetail, String cdnUrl, Long parentId) {
        return remoteReference.loadJztHomeworkNotice(newHomework, studentDetail, cdnUrl, parentId);
    }

    @Override
    public MapMessage loadDiagnosisHabitDetail(String homeworkId) {
        return remoteReference.loadDiagnosisHabitDetail(homeworkId);
    }

    @Override
    public MapMessage clazzWordTeachModuleDetail(Long teacherId, String hid, String stoneId, WordTeachModuleType wordTeachModuleType) {
        return remoteReference.clazzWordTeachModuleDetail(teacherId, hid, stoneId, wordTeachModuleType);
    }

    @Override
    public MapMessage studentImageTextRhymeDetail(String homeworkId, Long studentId, String stoneDataId, String chapterId) {
        return remoteReference.studentImageTextRhymeDetail(homeworkId, studentId, stoneDataId, chapterId);
    }

    @Override
    public MapMessage fetchOcrHomeworkStudentDetail(String hid, Teacher teacher) {
        return remoteReference.fetchOcrHomeworkStudentDetail(hid, teacher);
    }

    @Override
    public OcrHomeworkShareReport processOcrHomeworkShareReport(String newHomeworkId, User user, String cdnUrl) {
        return remoteReference.processOcrHomeworkShareReport(newHomeworkId, user, cdnUrl);
    }

    @Override
    public MapMessage loadOcrHomeworkDetail(List<String> homeworkIds) {
        return remoteReference.loadOcrHomeworkDetail(homeworkIds);
    }
}
