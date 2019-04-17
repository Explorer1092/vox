package com.voxlearning.utopia.service.piclisten.api;


import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.piclisten.api.entity.MiniProgramCheck;

import java.util.concurrent.TimeUnit;

@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
@ServiceVersion(version = "20180521")
public interface MiniProgramCheckService extends IPingable {

    boolean isChecked(Long uid);

    MapMessage doCheck(Long uid, Long pid);

    Long  getTodayCheckCount();

    int getWeekContinuousCheckCount(Long uid);

    int getTotalCheckCount(Long uid);

    MapMessage loadCheckData(Long uid);

    MiniProgramCheck loadByUid(Long uid);
}
