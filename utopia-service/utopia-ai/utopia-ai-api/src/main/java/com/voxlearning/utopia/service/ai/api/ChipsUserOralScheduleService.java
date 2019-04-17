package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.entity.ChipsUserOralTestSchedule;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author guangqing
 * @since 2019/3/12
 */
@ServiceVersion(version = "20190312")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsUserOralScheduleService {

    /**
     * 短期课
     * @param userId
     * @param testBeginTime
     * @param testEndTime
     * @return
     */
    MapMessage updateChipsUserOralTestSchedule(Long userId, Date testBeginTime, Date testEndTime);

    List<ChipsUserOralTestSchedule> loadByClazzId(Long clazzId);
}
