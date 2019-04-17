package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.WordRecognitionSummaryResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180728")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface WordRecognitionHomeworkService extends IPingable {

    @Idempotent
    List<WordRecognitionSummaryResult> getWordRecognitionSummaryInfo(String homeworkId, Long studentId);
}
