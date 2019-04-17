package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSummaryResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author guoqiang.li
 * @since 2017/10/31
 */
@ServiceVersion(version = "20181204")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface DubbingHomeworkService extends IPingable {
    /**
     * 配音摘要信息
     */
    @Idempotent
    List<DubbingSummaryResult> getDubbingSummerInfo(String homeworkId, Long studentId, String objectiveConfigType);

    @Idempotent
    List<DubbingSummaryResult> getVacationDubbingSummerInfo(String homeworkId, Long studentId, String objectiveConfigType);

    @Idempotent
    List<DubbingSummaryResult> getLiveCastDubbingSummerInfo(String homeworkId, Long studentId);
}
