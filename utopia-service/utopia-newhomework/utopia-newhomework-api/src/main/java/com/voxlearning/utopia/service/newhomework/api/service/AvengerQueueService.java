package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalStudentHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.avenger.AvengerHomework;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用于各种上报
 *
 * @author xuesong.zhang
 * @since 2017/6/15
 */
@ServiceVersion(version = "20170615")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries()
@CyclopsMonitor("utopia")
public interface AvengerQueueService extends IPingable {

    @NoResponseWait
    void sendHomeworkProcessResultList(List<JournalNewHomeworkProcessResult> results);

    @NoResponseWait
    void sendHomework(AvengerHomework avengerHomework);

    @NoResponseWait
    void sendJournalStudentHomework(JournalStudentHomework journalStudentHomework);
}
