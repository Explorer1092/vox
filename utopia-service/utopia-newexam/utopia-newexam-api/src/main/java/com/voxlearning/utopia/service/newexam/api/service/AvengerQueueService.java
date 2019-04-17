package com.voxlearning.utopia.service.newexam.api.service;

import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newexam.api.entity.AvengerNewExam;
import com.voxlearning.utopia.service.newexam.api.entity.JournalNewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.entity.JournalStudentNewExam;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 数据上报
 * @author: Mr_VanGogh
 * @date: 2019/4/10 下午4:26
 */
@ServiceVersion(version = "20190410")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries()
@CyclopsMonitor("utopia")
public interface AvengerQueueService extends IPingable {

    @NoResponseWait
    void sendExam(AvengerNewExam avengerNewExam);

    @NoResponseWait
    void sendExamProcessResultList(List<JournalNewExamProcessResult> results);

    @NoResponseWait
    void sendJournalStudentExam(JournalStudentNewExam journalStudentNewExam);

    @NoResponseWait
    void sendJournalNewExamProcessResultProducer(List<JournalNewExamProcessResult> results);
}
