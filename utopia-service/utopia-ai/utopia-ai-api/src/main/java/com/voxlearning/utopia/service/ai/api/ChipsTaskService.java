package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190328")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsTaskService extends IPingable {


    MapMessage processDrawingTaskJoin(Long drawingTaskId, String openId, String userAnswer);

    MapMessage processPageRedDotRead(String pageCode, Long userId);
}
