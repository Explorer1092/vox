package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.DPLiveCastHomeworkReportLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.*;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DPLiveCastHomeworkReportLoaderClient implements DPLiveCastHomeworkReportLoader {

    @ImportService(interfaceClass = DPLiveCastHomeworkReportLoader.class)
    private DPLiveCastHomeworkReportLoader remoteReference;

    @Override
    public LiveHomeworkReport obtainLiveHomeworkReport(String hid) {
        return remoteReference.obtainLiveHomeworkReport(hid);
    }

    @Override
    public Map<String, LiveCastHomework> obtainLiveHomeworkMap(List<String> hids) {
        return remoteReference.obtainLiveHomeworkMap(hids);
    }


    @Override
    public LiveHomeworkBriefPage pageHomeworkReportListByGroupIds(Collection<Long> groupIds, Integer page, Integer size) {
        return remoteReference.pageHomeworkReportListByGroupIds(groupIds, page, size);
    }

    @Override
    public StudentLiveHomeworkDetail fetchStudentLiveHomeworkDetail(String hid, Long userId) {
        return remoteReference.fetchStudentLiveHomeworkDetail(hid, userId);
    }

    @Override
    public Map<String, LiveCastHomeworkDetail> fetchLiveCastHomeworkDetail(Collection<String> homeworkIds) {
        return remoteReference.fetchLiveCastHomeworkDetail(homeworkIds);
    }

    @Override
    public MapMessage loadLiveCastHomeworkReportDetail(String homeworkId) {
        return remoteReference.loadLiveCastHomeworkReportDetail(homeworkId);
    }

    @Override
    public MapMessage loadLiveCastHomeworkReportDetail(String homeworkId, Long studentId) {
        return remoteReference.loadLiveCastHomeworkReportDetail(homeworkId, studentId);
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
    public MapMessage personalReadingDetail(String homeworkId, Long studentId, String readingId) {
        return remoteReference.personalReadingDetail(homeworkId, studentId, readingId);
    }

    @Override
    public MapMessage personalReadingDetail(String homeworkId, Long studentId, String readingId, ObjectiveConfigType type) {

        return remoteReference.personalReadingDetail(homeworkId, studentId, readingId,type);
    }

    @Override
    public MapMessage fetchSubjectiveQuestion(String hid, ObjectiveConfigType objectiveConfigType, String qid) {
        return remoteReference.fetchSubjectiveQuestion(hid, objectiveConfigType, qid);
    }

    @Override
    public Boolean judgeLiveHomeworkSubjective(String hid) {
        return remoteReference.judgeLiveHomeworkSubjective(hid);
    }

    @Override
    public Boolean judgeLiveHomeworkObjective(String hid) {
        return remoteReference.judgeLiveHomeworkObjective(hid);
    }

    @Override
    public MapMessage loadExamQuestionByIds(String data) {
        return remoteReference.loadExamQuestionByIds(data);
    }

    @Override
    public Integer judgeLiveHomeworkType(String hid) {
        return remoteReference.judgeLiveHomeworkType(hid);
    }

    @Override
    public MapMessage personalDubbingDetail(String homeworkId, Long studentId, String dubbingId, Long teacherId) {

        return remoteReference.personalDubbingDetail(homeworkId,studentId,dubbingId,teacherId);
    }

    @Override
    public MapMessage loadHomeworkQuestionsAnswer(String objectiveConfigType, String homeworkId, Long studentId, Integer categoryId, String lessonId, String videoId) {

        return remoteReference.loadHomeworkQuestionsAnswer(objectiveConfigType, homeworkId, studentId, categoryId, lessonId, videoId);
    }

}
