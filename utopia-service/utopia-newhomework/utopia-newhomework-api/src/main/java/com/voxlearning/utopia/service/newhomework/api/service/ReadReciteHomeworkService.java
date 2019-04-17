package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.ReadReciteSummaryResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by tanguohong on 2017/6/2.
 */
@ServiceVersion(version = "20180602")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface ReadReciteHomeworkService extends IPingable {

    @Idempotent
    List<ReadReciteSummaryResult> getReadReciteSummaryInfo(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId);

    @Idempotent
    List<ReadReciteSummaryResult> getVacationReadReciteSummaryInfo(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId);
}
