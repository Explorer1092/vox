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

package com.voxlearning.utopia.service.newexam.consumer.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.api.service.NewExamService;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author guoqiang.li
 * @since 2016/3/8
 */
public class NewExamServiceClient{

    @Getter
    @ImportService(interfaceClass = NewExamService.class)
    private NewExamService remoteReference;

    public List<Map<String, Object>> loadExamsCanBeEntered(StudentDetail studentDetail, School school, ExRegion exRegion, Integer beforeExamStartMinutes) {
        return remoteReference.loadExamsCanBeEntered(studentDetail, school, exRegion, beforeExamStartMinutes);
    }


    public MapMessage handlerStudentExaminationAuthority(Long sid, String newExamId, boolean makeUp) {
        return remoteReference.handlerStudentExaminationAuthority(sid, newExamId, makeUp);
    }

    /**
     * 中学dubbo-proxy用到，勿删
     */
    public List<Map<String, Object>> loadExamsCanBeEnteredByStudentId(Long studentId) {
        return remoteReference.loadExamsCanBeEnteredByStudentId(studentId);
    }

    public MapMessage loadAllExams(StudentDetail studentDetail, School school, ExRegion exRegion) {
        return remoteReference.loadAllExams(studentDetail, school, exRegion);
    }

    /**
     * 中学dubbo-proxy用到，勿删
     */
    public MapMessage loadAllExamsByStudentId(Long studentId) {
        return remoteReference.loadAllExamsByStudentId(studentId);
    }

    public MapMessage registerNewExam(StudentDetail studentDetail, School school, ExRegion exRegion, String newExamId, String clientType, String clientName) {
        return remoteReference.registerNewExam(studentDetail, school, exRegion, newExamId, clientType, clientName);
    }

    public MapMessage unRegisterNewExam(StudentDetail studentDetail, School school, ExRegion exRegion, String newExamId, String clientType, String clientName) {
        return remoteReference.unRegisterNewExam(studentDetail, school, exRegion, newExamId, clientType, clientName);
    }

    public MapMessage processorNewExamResult(NewExamResultContext newExamResultContext) {
        return remoteReference.processorNewExamResult(newExamResultContext);
    }

    public MapMessage submitNewExam(String newExamId, Long userId, String clientType, String clientName) {
        return remoteReference.submitNewExam(newExamId, userId, clientType, clientName);
    }

    public MapMessage loadQuestionAnswer(String newExamId, Long studentId, Boolean includeStandardAnswer) {
        return remoteReference.loadQuestionAnswer(newExamId, studentId, includeStandardAnswer);
    }

    public MapMessage index(String newExamId, Long studentId) {
        return remoteReference.index(newExamId, studentId);
    }

    public MapMessage enterExam(String newExamId, StudentDetail studentDetail, String cdnUrl, String clientType, String clientName) {
        return remoteReference.enterExam(newExamId, studentDetail, cdnUrl, clientType, clientName);
    }

    public MapMessage viewExam(String newExamId, StudentDetail studentDetail, String cdnUrl) {
        return remoteReference.viewExam(newExamId, studentDetail, cdnUrl);
    }

    public MapMessage loadNewExamDetail(String newExamId, StudentDetail studentDetail) {
        return remoteReference.loadNewExamDetail(newExamId, studentDetail);
    }

    public MapMessage correctNewExam(CorrectNewExamContext correctNewExamContext) {
        return remoteReference.correctNewExam(correctNewExamContext);
    }

    public MapMessage resetScore(Map<String, Object> pram) {
        return remoteReference.resetScore(pram);
    }

    public MapMessage newResetScore(String param) {
        return remoteReference.newResetScore(param);
    }

    public MapMessage loadPaperList(String bookId, Teacher teacher) {
        return remoteReference.loadPaperList(bookId, teacher);
    }

    public MapMessage assignNewExam(Teacher teacher, Map<String, Object> source) {
        return remoteReference.assignNewExam(teacher, source);
    }

    public MapMessage crmSubmitNewExam(String newExamId, Long userId){
        return remoteReference.crmSubmitNewExam(newExamId, userId);
    }

    public MapMessage loadTeacherClazzListNew(Set<Long> teacherIds) {
        return remoteReference.loadTeacherClazzListNew(teacherIds);
    }


    public MapMessage loadTeacherClazzList(Set<Long> teacherIds) {
        return remoteReference.loadTeacherClazzList(teacherIds);
    }

    public MapMessage deleteNewExam(Teacher teacher, String newExamId) {
        return remoteReference.deleteNewExam(teacher, newExamId);
    }

    public MapMessage loadAppIndexData(Teacher teacher) {
        return remoteReference.loadAppIndexData(teacher);
    }

    public MapMessage shareIndependentReport(Teacher teacher, String newExamId) {
        return remoteReference.shareIndependentReport(teacher, newExamId);
    }

    public MapMessage handlerStudentExaminationAuthority(Long sid, String newExamId) {
        return remoteReference.handlerStudentExaminationAuthority(sid, newExamId);
    }

    public MapMessage restoreData(List<String> newExamResultIds) {
        return remoteReference.restoreData(newExamResultIds);
    }

    public MapMessage resetOralQuestionScoreV2(String newExamId, String questionId, String paperDocId, List<Long> userIds){
        return remoteReference.resetOralQuestionScoreV2(newExamId,questionId,paperDocId,userIds);
    }

    public MapMessage loadUnitTestPaperList(String unitId, Long teacherId) {
        return remoteReference.loadUnitTestPaperList(unitId, teacherId);
    }

    public MapMessage previewUnitTest(String paperId) {
        return remoteReference.previewUnitTest(paperId);
    }

    public MapMessage assignUnitTest(Teacher teacher, Map<String, Object> source) {
        return remoteReference.assignUnitTest(teacher, source);
    }

    public MapMessage adjustUnitTest(Long teacherId, String newExamId, Date end) {
        return remoteReference.adjustUnitTest(teacherId, newExamId, end);
    }

    public MapMessage loadStudentUnitTestHistoryList(StudentDetail studentDetail) {
        return remoteReference.loadStudentUnitTestHistoryList(studentDetail);
    }

    public MapMessage loadStudentIndexUnitTestList(StudentDetail studentDetail) {
        return remoteReference.loadStudentIndexUnitTestList(studentDetail);
    }
}
