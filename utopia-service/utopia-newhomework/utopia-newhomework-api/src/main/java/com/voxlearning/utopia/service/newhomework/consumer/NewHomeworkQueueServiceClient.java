package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalStudentHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkSyllable;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.ViewHintReq;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkQueueService;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;

import java.util.List;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2016/8/22
 */
public class NewHomeworkQueueServiceClient implements NewHomeworkQueueService {

    @ImportService(interfaceClass = NewHomeworkQueueService.class)
    private NewHomeworkQueueService remoteReference;

    @Override
    public void sendUpdateTotalAssignmentRecordMessage(Subject subject, List<NewHomeworkPracticeContent> practices, Integer clazzGroupSize) {
        remoteReference.sendUpdateTotalAssignmentRecordMessage(subject, practices, clazzGroupSize);
    }

    @Override
    public void saveJournalNewHomeworkProcessResults(List<JournalNewHomeworkProcessResult> results) {
        remoteReference.saveJournalNewHomeworkProcessResults(results);
    }

    @Override
    public void saveJournalStudentHomework(JournalStudentHomework journalStudentHomework) {
        remoteReference.saveJournalStudentHomework(journalStudentHomework);
    }

    @Override
    public void saveHomeworkSyllable(List<NewHomeworkSyllable> results) {
        remoteReference.saveHomeworkSyllable(results);
    }

    @Override
    public void saveSelfStudyWordsIncreaseHomework(Long clazzGroupId, Long studentId, Map<String, Map<String, List<String>>> bookToKpMap) {
        remoteReference.saveSelfStudyWordsIncreaseHomework(clazzGroupId, studentId, bookToKpMap);
    }

    @Override
    public void interventionViewhintProducer(Long userId, ViewHintReq request) {
        remoteReference.interventionViewhintProducer(userId, request);
    }
}
