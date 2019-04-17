package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.constants.MizarRatingStatus;

import java.util.concurrent.TimeUnit;

/**
 * Created by Yuechen Wang on 16/9/6.
 */

@ServiceVersion(version = "1.0.DEV")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarRatingService {

    MapMessage updateRatingStatus(String rating, MizarRatingStatus status);

}
