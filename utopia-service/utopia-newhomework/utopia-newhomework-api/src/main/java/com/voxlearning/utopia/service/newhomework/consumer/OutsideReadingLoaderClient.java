package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.OutsideReadingLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OutsideReadingLoaderClient implements OutsideReadingLoader {

    @ImportService(interfaceClass = OutsideReadingLoader.class)
    private OutsideReadingLoader hydraRemoteReference;

    @Override
    public MapMessage loadBookshelf(Long userId, String cdnUrl) {
        return hydraRemoteReference.loadBookshelf(userId, cdnUrl);
    }

    @Override
    public Map<String, Boolean> loadOutsideReadingStatus(Long userId) {
        return hydraRemoteReference.loadOutsideReadingStatus(userId);
    }

    @Override
    public List<OutsideReading> loadUnFinishedOutsideReadings(Long userId) {
        return hydraRemoteReference.loadUnFinishedOutsideReadings(userId);
    }

    @Override
    public MapMessage loadStudentBookDetail(Long userId, String outsideReadingId) {
        return hydraRemoteReference.loadStudentBookDetail(userId, outsideReadingId);
    }

    @Override
    public OutsideReading findOutsideReadingById(String outsideReadingId){
        return hydraRemoteReference.findOutsideReadingById(outsideReadingId);
    }

    @Override
    public Map<Long, List<OutsideReading>> loadOutsideReadingByGroupId(Collection<Long> groupIds){
        return hydraRemoteReference.loadOutsideReadingByGroupId(groupIds);
    }

    @Override
    public MapMessage fetchGoldenWordsList(Long userId, String label, Integer pageNum, Integer pageSize) {
        return hydraRemoteReference.fetchGoldenWordsList(userId, label, pageNum, pageSize);
    }

    @Override
    public MapMessage loadQuestions(String outsideReadingId, String missionId){
        return hydraRemoteReference.loadQuestions(outsideReadingId, missionId);
    }

    @Override
    public MapMessage loadQuestionsAnswer(Long studentId, String outsideReadingId, String missionId){
        return hydraRemoteReference.loadQuestionsAnswer(studentId, outsideReadingId, missionId);
    }

    @Override
    public MapMessage goldenWordsIndex(Long userId, String outsideReadingId, String missionId) {
        return hydraRemoteReference.goldenWordsIndex(userId, outsideReadingId, missionId);
    }

    @Override
    public MapMessage fetchMissionAchievement(Long userId, String outsideReadingId, String missionId) {
        return hydraRemoteReference.fetchMissionAchievement(userId, outsideReadingId, missionId);
    }

    @Override
    public MapMessage fetchAchievement(Long userId, String cdnUrl) {
        return hydraRemoteReference.fetchAchievement(userId, cdnUrl);
    }

    @Override
    public MapMessage loadReportBookList(Long teacherId, Long groupId) {
        return hydraRemoteReference.loadReportBookList(teacherId, groupId);
    }

    @Override
    public MapMessage loadReportBookDetail(String readingId) {
        return hydraRemoteReference.loadReportBookDetail(readingId);
    }

    @Override
    public MapMessage fetchAnswerDetail(String readingId, String cdnUrl) {
        return hydraRemoteReference.fetchAnswerDetail(readingId, cdnUrl);
    }

    @Override
    public MapMessage fetchAnswerShareDetail(String readingId, String processResultId) {
        return hydraRemoteReference.fetchAnswerShareDetail(readingId, processResultId);
    }

    @Override
    public MapMessage loadReportClazzAchievement(Long groupId) {
        return hydraRemoteReference.loadReportClazzAchievement(groupId);
    }

    @Override
    public MapMessage crmLoadOutsideReadingsByGroupId(Long groupId) {
        return hydraRemoteReference.crmLoadOutsideReadingsByGroupId(groupId);
    }
}
