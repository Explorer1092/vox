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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.api.client.INewExamReportLoaderClient;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamStudent;
import com.voxlearning.utopia.service.newexam.api.loader.NewExamReportLoader;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamReportForClazz;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamReportForStudent;
import com.voxlearning.utopia.service.newexam.api.mapper.report.TamExamInfo;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.Getter;

import java.util.List;
import java.util.Map;


public class NewExamReportLoaderClient implements INewExamReportLoaderClient {

    @Getter
    @ImportService(interfaceClass = NewExamReportLoader.class)
    private NewExamReportLoader remoteReference;

    public MapMessage pageUnifyExamList(Long teacherId, Long clazzId, Subject subject, Integer iDisplayLength, Integer iDisplayStart) {
        return remoteReference.pageUnifyExamList(teacherId, clazzId, subject, iDisplayLength, iDisplayStart);
    }

    @Override
    public MapMessage newPageUnifyExamList(Teacher teacher, Long groupId, Integer iDisplayLength, Integer iDisplayStart) {
        return remoteReference.newPageUnifyExamList(teacher, groupId, iDisplayLength, iDisplayStart);
    }

    public MapMessage pageUnitTestList(Teacher teacher, Subject subject, List<Long> groupIds, Integer iDisplayLength, Integer iDisplayStart) {
        return remoteReference.pageUnitTestList(teacher, subject, groupIds, iDisplayLength, iDisplayStart);
    }

    @Override
    public MapMessage crmUnifyExamList(Long teacherId, Long clazzId, Subject subject, Long groupId) {
        return remoteReference.crmUnifyExamList(teacherId, clazzId, subject, groupId);
    }

    public MapMessage examDetailForClazz(Teacher teacher, String newExamId, Long clazzId) {
        return remoteReference.examDetailForClazz(teacher, newExamId, clazzId);
    }

    public MapMessage examDetailForStudent(Teacher teacher, String newExamId, Long clazzId) {
        return remoteReference.examDetailForStudent(teacher, newExamId, clazzId);
    }

    public MapMessage independentExamDetailForClazz(Teacher teacher, String newExamId) {
        return remoteReference.independentExamDetailForClazz(teacher, newExamId);
    }

    public MapMessage independentExamDetailForStudent(Teacher teacher, String newExamId) {
        return remoteReference.independentExamDetailForStudent(teacher, newExamId);
    }

    @Override
    public MapMessage fetchTeacherClazzInfo(Teacher teacher) {
        return remoteReference.fetchTeacherClazzInfo(teacher);
    }

    @Override
    public MapMessage independentExamDetailForParent(String newExamId, Long studentId) {
        return remoteReference.independentExamDetailForParent(newExamId, studentId);
    }

    @Override
    public MapMessage independentExamDetailForShare(String newExamId) {
        return remoteReference.independentExamDetailForShare(newExamId);
    }

    @Override
    public MapMessage loadNewExamParentReport(String newExamId, Long StudentId) {
        return remoteReference.loadNewExamParentReport(newExamId, StudentId);
    }

    @Override
    public List<RptMockNewExamStudent> getStudentAchievement(String examId){
        return remoteReference.getStudentAchievement(examId);
    }

    @Override
    public List<RptMockNewExamStudent> getStudentAchievement(ExRegion region, String examId) {
        return remoteReference.getStudentAchievement(region, examId);
    }

    @Override
    public List<RptMockNewExamStudent> getStudentAchievement(String examId, Integer clazzId) {
        return remoteReference.getStudentAchievement(examId, clazzId);
    }

    @Override
    public List<Map<String, Object>> getRegionStatistic(String examId, ExRegion exRegion, String paperId) {
        return remoteReference.getRegionStatistic(examId, exRegion, paperId);
    }

    @Override
    public List<Map<String, Object>> getSchoolStatistic(String examId, String paperId, ExRegion exRegion, String countyId) {
        return remoteReference.getSchoolStatistic(examId, paperId, exRegion, countyId);
    }

    @Override
    public List<Map<String, Object>> getClassStatistic(String examId, String paperId, ExRegion exRegion, String schoolId) {
        return remoteReference.getClassStatistic(examId, paperId, exRegion, schoolId);
    }

    @Override
    public NewExamReportForClazz crmReceiveNewExamReportForClazz(Teacher teacher, String newExamId, Long clazzId) {
        return remoteReference.crmReceiveNewExamReportForClazz(teacher, newExamId, clazzId);
    }

    @Override
    public NewExamReportForStudent crmReceiveNewExamReportForStudent(String newExamId) {
        return remoteReference.crmReceiveNewExamReportForStudent(newExamId);
    }

    @Override
    public MapMessage fetchNewExamSingleQuestionDimension(Teacher teacher, String newExamId, Long clazzId, String paperId, String questionId, int subIndex) {
        return remoteReference.fetchNewExamSingleQuestionDimension(teacher, newExamId, clazzId, paperId, questionId, subIndex);
    }

    @Override
    public MapMessage fetchNewExamPaperInfo(String newExamId, Long clazzId) {
        return remoteReference.fetchNewExamPaperInfo(newExamId, clazzId);
    }

    @Override
    public MapMessage fetchPaperInfo(List<String> paperIds) {
        return remoteReference.fetchPaperInfo(paperIds);
    }

    @Override
    public MapMessage fetchNewExamPaperQuestionInfo(String newExamId, Long sid) {
        return remoteReference.fetchNewExamPaperQuestionInfo(newExamId, sid);
    }

    @Override
    public MapMessage fetchNewExamPaperQuestionInfo(String newExamId, String paperId) {
        return remoteReference.fetchNewExamPaperQuestionInfo(newExamId, paperId);
    }

    @Override
    public MapMessage fetchPaperQuestionInfo(String paperId) {
        return remoteReference.fetchPaperQuestionInfo(paperId);
    }

    @Override
    public MapMessage fetchNewExamPaperQuestionAnswerInfo(Teacher teacher, String newExamId, Long clazzId, String paperId) {
        return remoteReference.fetchNewExamPaperQuestionAnswerInfo(teacher, newExamId, clazzId, paperId);
    }

    @Override
    public MapMessage paperClazzAnswerDetail(Teacher teacher, String newExamId, Long clazzId, String paperId) {
        return remoteReference.paperClazzAnswerDetail(teacher, newExamId, clazzId, paperId);
    }

    @Override
    public MapMessage fetchNewExamPaperQuestionPersonalAnswerInfo(String newExamId, Long userId) {
        return remoteReference.fetchNewExamPaperQuestionPersonalAnswerInfo(newExamId, userId);
    }

    @Override
    public MapMessage fetchNewExamPaperStudentAnswerInfo(String newExamId, Long userId) {
        return remoteReference.fetchNewExamPaperStudentAnswerInfo(newExamId, userId);
    }


    @Override
    public MapMessage fetchNewExamQuestionReport(Teacher teacher, String newExamId, Long clazzId) {
        return remoteReference.fetchNewExamQuestionReport(teacher, newExamId, clazzId);
    }

    @Override
    public MapMessage fetchNewExamAttendanceReport(Teacher teacher, String newExamId, Long clazzId) {
        return remoteReference.fetchNewExamAttendanceReport(teacher, newExamId, clazzId);
    }


    @Override
    public MapMessage fetchNewExamStudentReport(Teacher teacher, String newExamId, Long clazzId) {
        return remoteReference.fetchNewExamStudentReport(teacher, newExamId, clazzId);
    }

    @Override
    public MapMessage fetchNewExamStatisticsReport(Teacher teacher, String newExamId, Long clazzId) {
        return remoteReference.fetchNewExamStatisticsReport(teacher, newExamId, clazzId);
    }

    @Override
    public TamExamInfo fetchTamExamInfo(String newExamId) {
        return remoteReference.fetchTamExamInfo(newExamId);
    }

    @Override
    public void studentViewExamReportKafka(String examId, Long userId, String actionRefer) {
        remoteReference.studentViewExamReportKafka(examId, userId, actionRefer);
    }

    @Override
    public MapMessage shareReport(String newExamId, Long clazzId){
        return remoteReference.shareReport(newExamId, clazzId);
    }

    @Override
    public MapMessage loadUnitTestDetail(List<String> newExamIds) {
        return remoteReference.loadUnitTestDetail(newExamIds);
    }

    @Override
    public MapMessage loadUnitTestAdjustDetail(String examId, Long teacherId) {
        return remoteReference.loadUnitTestAdjustDetail(examId, teacherId);
    }

    @Override
    public MapMessage fetchUnitTestTeacherClazzInfo(Teacher teacher) {
        return remoteReference.fetchUnitTestTeacherClazzInfo(teacher);
    }
}
