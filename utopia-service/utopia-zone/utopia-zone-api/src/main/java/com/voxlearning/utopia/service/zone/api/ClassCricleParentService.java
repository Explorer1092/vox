package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.concurrent.TimeUnit;

/**
 * @author chensn
 * @date 2018-12-24 11:17
 */
@ServiceVersion(version = "20181023")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClassCricleParentService {

    MapMessage orderCommit(Long userId, Integer type, String subject);

    MapMessage loadOrderRecord(Long userId,Integer type);
}
