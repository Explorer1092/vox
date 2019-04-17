package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181130")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClazzZoneCommonService {

    String dispatch(Map<String, Object> map);

}
