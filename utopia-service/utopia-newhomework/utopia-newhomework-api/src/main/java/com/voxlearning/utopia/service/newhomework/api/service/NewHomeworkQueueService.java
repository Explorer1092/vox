package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalStudentHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkSyllable;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.ViewHintReq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2016/10/31
 */
@ServiceVersion(version = "20180801")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries()
@CyclopsMonitor("utopia")
public interface NewHomeworkQueueService extends IPingable {

    @NoResponseWait
    void sendUpdateTotalAssignmentRecordMessage(Subject subject, List<NewHomeworkPracticeContent> practices, Integer clazzGroupSize);

    @NoResponseWait
    void saveJournalNewHomeworkProcessResults(List<JournalNewHomeworkProcessResult> results);

    @NoResponseWait
    void saveJournalStudentHomework(JournalStudentHomework journalStudentHomework);

    @NoResponseWait
    void saveHomeworkSyllable(List<NewHomeworkSyllable> results);

    @NoResponseWait
    void saveSelfStudyWordsIncreaseHomework(Long clazzGroupId, Long studentId, Map<String, Map<String, List<String>>> bookToKpMap);

    @NoResponseWait
    void interventionViewhintProducer(Long userId, ViewHintReq request);
}
