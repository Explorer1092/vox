package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2016.12.29")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ZoneStudyMasterService {

    @Idempotent
    @ServiceRetries(retries = 1)
    Map<Long, Integer> getMonthStudyMasterCount(Collection<Long> userIds, HomeworkType type);

    void increaseMonthStudyMasterCount(Collection<Long> userIds, HomeworkType type);
}
