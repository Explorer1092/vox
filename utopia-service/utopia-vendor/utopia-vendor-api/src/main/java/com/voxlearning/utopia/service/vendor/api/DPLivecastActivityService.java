package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.concurrent.TimeUnit;

/**
 * @author jiangpeng
 * @since 2017-11-28 下午2:56
 **/
@ServiceVersion(version = "2017.11.28")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPLivecastActivityService  {

    MapMessage studentAddLotteryChance(Long studentId, String subject);
}
