package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.OralStudentQuestionResp;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.*;
import com.voxlearning.utopia.service.newhomework.api.service.DiagnoseReportService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Collection;
import java.util.List;

public class DiagnoseReportServiceClient implements DiagnoseReportService {

    @ImportService(interfaceClass = DiagnoseReportService.class)
    private DiagnoseReportService diagnoseReportService;

    @Override
    public List<ClazzGroupInfoResp> fetchClazzInfo(Collection<Long> teacherIds) {
        return diagnoseReportService.fetchClazzInfo(teacherIds);
    }

    @Override
    public PageImpl<DiagnoseReportReportListResp> fetchReportList(Long groupId, Pageable pageable, Subject subject) {
        return diagnoseReportService.fetchReportList(groupId, pageable, subject);
    }

    @Override
    public DiagnoseReportDetailResp fetchReportDetail(String hid, ObjectiveConfigType configType, String questionBoxId) {
        return diagnoseReportService.fetchReportDetail(hid, configType, questionBoxId);
    }

    @Override
    public List<OralStudentQuestionResp> oralStudentQuestionDetail(String hid, ObjectiveConfigType configType, String questionId) {
        return diagnoseReportService.oralStudentQuestionDetail(hid, configType, questionId);
    }

    @Override
    public long countInterventionGraspQuestion(NewHomework newHomework, Long studentId) {
        return diagnoseReportService.countInterventionGraspQuestion(newHomework, studentId);
    }

    @Override
    public IntelligentTeachingRecommendResp fetchIntelligentTeachingRecommend(String homeworkId) {
        return diagnoseReportService.fetchIntelligentTeachingRecommend(homeworkId);
    }

    @Override
    public IntelligentTeachingReport fetchIntelligentTeachingReport(String homeworkId) {
        return diagnoseReportService.fetchIntelligentTeachingReport(homeworkId);
    }
}
